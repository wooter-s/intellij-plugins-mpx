// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.analyzer;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.util.SmartList;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.io.URLUtil;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsView;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsViewSettings;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import com.jetbrains.lang.dart.util.DartBuildFileUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartServerRootsHandler {

  private final @NotNull Project myProject;

  private final List<String> myIncludedRootPaths = new SmartList<>();
  private final List<String> myExcludedRootPaths = new SmartList<>();

  public DartServerRootsHandler(@NotNull Project project) {
    myProject = project;
  }

  void onServerStopped() {
    myIncludedRootPaths.clear();
    myExcludedRootPaths.clear();
  }

  void onServerStarted() {
    assert (myIncludedRootPaths.isEmpty());
    assert (myExcludedRootPaths.isEmpty());

    scheduleDartRootsUpdate(() -> {
      DartAnalysisServerService das = DartAnalysisServerService.getInstance(myProject);
      das.updateCurrentFile();
      das.updateVisibleFiles();
    });
  }

  void scheduleDartRootsUpdate(@Nullable Runnable onSuccess) {
    ReadAction.nonBlocking(() -> calcIncludedAndExcludedDartRootPaths())
      .coalesceBy(this)
      .expireWith(DartAnalysisServerService.getInstance(myProject))
      .finishOnUiThread(ModalityState.nonModal(), includedAndExcludedRootPaths -> {
        if (includedAndExcludedRootPaths != null) {
          sendSetAnalysisRootsRequest(includedAndExcludedRootPaths.first, includedAndExcludedRootPaths.second);

          if (onSuccess != null) {
            onSuccess.run();
          }
        }
      })
      .submit(AppExecutorUtil.getAppExecutorService());
  }

  private @Nullable Couple<List<String>> calcIncludedAndExcludedDartRootPaths() {
    final DartSdk sdk = DartSdk.getDartSdk(myProject);
    if (sdk == null || !DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) {
      DartAnalysisServerService.getInstance(myProject).stopServer();
      return null;
    }

    final List<String> newIncludedRootPaths = new SmartList<>();
    final List<String> newExcludedRootPaths = new SmartList<>();

    final boolean isPackageScopedAnalysis =
      DartProblemsView.getScopeAnalysisMode(myProject) == DartProblemsViewSettings.ScopedAnalysisMode.DartPackage;

    if (isPackageScopedAnalysis) {
      final VirtualFile currentFile = DartProblemsView.getInstance(myProject).getCurrentFile();
      if (currentFile == null ||
          ProjectFileIndex.getInstance(myProject).isInLibraryClasses(currentFile) ||
          !currentFile.isInLocalFileSystem()) {
        return null; // keep server roots as is until another file is open
      }

      newIncludedRootPaths.add(getEnclosingDartPackageDirectoryPath(currentFile));
    }

    final String dotIdeaPath = PathUtil.getParentPath(StringUtil.notNullize(myProject.getProjectFilePath()));
    if (dotIdeaPath.endsWith("/.idea")) {
      newExcludedRootPaths.add(dotIdeaPath);
    }

    for (Module module : DartSdkLibUtil.getModulesWithDartSdkEnabled(myProject)) {
      for (ContentEntry contentEntry : ModuleRootManager.getInstance(module).getContentEntries()) {
        final String contentEntryUrl = contentEntry.getUrl();
        if (contentEntryUrl.startsWith(URLUtil.FILE_PROTOCOL + URLUtil.SCHEME_SEPARATOR)) {
          if (!isPackageScopedAnalysis) {
            newIncludedRootPaths.add(VfsUtilCore.urlToPath(contentEntryUrl));
          }
          for (String excludedUrl : contentEntry.getExcludeFolderUrls()) {
            // Analysis Server knows about special 'packages' folders, IDE doesn't need to explicitly list them as excluded.
            if (excludedUrl.startsWith(contentEntryUrl) && !excludedUrl.endsWith("/packages")) {
              newExcludedRootPaths.add(VfsUtilCore.urlToPath(excludedUrl));
            }
          }
        }
      }
    }

    if (myIncludedRootPaths.equals(newIncludedRootPaths) && myExcludedRootPaths.equals(newExcludedRootPaths)) {
      return null;
    }

    return Couple.of(newIncludedRootPaths, newExcludedRootPaths);
  }

  private void sendSetAnalysisRootsRequest(@NotNull List<String> newIncludedRoots, @NotNull List<String> newExcludedRoots) {
    myIncludedRootPaths.clear();
    myExcludedRootPaths.clear();

    if (DartAnalysisServerService.getInstance(myProject).setAnalysisRoots(newIncludedRoots, newExcludedRoots)) {
      myIncludedRootPaths.addAll(newIncludedRoots);
      myExcludedRootPaths.addAll(newExcludedRoots);
    }
  }

  boolean isInIncludedRoots(@Nullable VirtualFile vFile) {
    if (vFile == null) return false;

    final DartProblemsViewSettings.ScopedAnalysisMode scopedAnalysisMode = DartProblemsView.getScopeAnalysisMode(myProject);
    if (scopedAnalysisMode == DartProblemsViewSettings.ScopedAnalysisMode.All) {
      final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
      if (fileIndex.isInLibraryClasses(vFile)) return true;

      final Module module = fileIndex.getModuleForFile(vFile);
      if (module != null && DartSdkLibUtil.isDartSdkEnabled(module)) {
        return true;
      }
      else if (vFile.getName().equals("AndroidManifest.xml")) {
        // These types of files can be part of an android module, not dart sdk enabled,
        // but should still be considered in the root for Dart Analysis errors and warnings.
        for (String root : myIncludedRootPaths) {
          if (vFile.getPath().startsWith(root + "/")) {
            return true;
          }
        }
      }
    }
    else if (scopedAnalysisMode == DartProblemsViewSettings.ScopedAnalysisMode.DartPackage) {
      for (String root : myIncludedRootPaths) {
        if (vFile.getPath().startsWith(root + "/")) {
          return true;
        }
      }
    }
    return false;
  }

  private @NotNull String getEnclosingDartPackageDirectoryPath(@NotNull VirtualFile vFile) {
    final VirtualFile pubspec = Registry.is("dart.projects.without.pubspec", false)
                                ? DartBuildFileUtil.findPackageRootBuildFile(myProject, vFile)
                                : PubspecYamlUtil.findPubspecYamlFile(myProject, vFile);
    // temp var to make sure that value is initialized
    final VirtualFile packageRoot;
    if (pubspec == null) {
      packageRoot = ProjectFileIndex.getInstance(myProject).getContentRootForFile(vFile, false);
    }
    else {
      packageRoot = pubspec.getParent();
    }

    if (packageRoot != null) {
      return packageRoot.getPath();
    }

    // As a fall-back, return the enclosing directory
    return vFile.getParent().getPath();
  }
}

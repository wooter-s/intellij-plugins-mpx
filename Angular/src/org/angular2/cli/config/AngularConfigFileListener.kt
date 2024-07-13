// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.config

import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.FileContentUtil
import com.intellij.util.asSafely
import org.angular2.cli.AngularCliUtil

class AngularConfigFileListener : BulkFileListener {
  override fun after(events: List<VFileEvent>) {
    for (event in events) {
      if (event !is VFileContentChangeEvent) continue
      val file = event.file
      if (!AngularCliUtil.isAngularJsonFile(file.name)) continue

      for (project in ProjectManager.getInstance().openProjects) {
        if (ProjectRootManager.getInstance(project).fileIndex.isInContent(file)) {
          ApplicationManager.getApplication().invokeLater {
            FileContentUtil.reparseFiles(project, FileEditorManager.getInstance(project).openFiles.filter { file ->
              file.fileType.asSafely<LanguageFileType>()?.language?.asSafely<JSLanguageDialect>()?.optionHolder?.isTypeScript == true
            }, false)
          }
          break
        }
      }
    }
  }
}
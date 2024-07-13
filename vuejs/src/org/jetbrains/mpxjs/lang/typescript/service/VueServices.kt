// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.typescript.service

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerState
import com.intellij.lang.typescript.lsp.JSServiceSetActivationRule
import com.intellij.lang.typescript.lsp.getTypeScriptServiceDirectory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.TestOnly
import org.jetbrains.mpxjs.context.isVueContext
import org.jetbrains.mpxjs.lang.html.VueFileType
import org.jetbrains.mpxjs.lang.html.isVueFile
import org.jetbrains.mpxjs.lang.typescript.service.volar.VolarExecutableDownloader
import org.jetbrains.mpxjs.options.VueServiceSettings
import org.jetbrains.mpxjs.options.getVueSettings


object VueServiceSetActivationRule : JSServiceSetActivationRule(VolarExecutableDownloader, null) {
  override fun isFileAcceptableForLspServer(file: VirtualFile): Boolean {
    if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.value(file)) return false

    return file.isVueFile || TypeScriptLanguageServiceUtil.ACCEPTABLE_TS_FILE.value(file)
  }

  override fun isProjectContext(project: Project, context: VirtualFile): Boolean {
    return isVueServiceContext(project, context)
  }

  override fun isEnabledInSettings(project: Project): Boolean {
    return when (getVueSettings(project).serviceType) {
      VueServiceSettings.AUTO, VueServiceSettings.VOLAR -> true
      VueServiceSettings.TS_SERVICE -> false
      VueServiceSettings.DISABLED -> false
    }
  }
}

private fun isVueServiceContext(project: Project, context: VirtualFile): Boolean {
  return context.fileType is VueFileType || isVueContext(context, project)
}

//<editor-fold desc="org.jetbrains.mpxjs.lang.typescript.service.classic.VueClassicTypeScriptService">

/**
 * Refers to the classic service that predates Volar.
 */
fun isVueClassicTypeScriptServiceEnabled(project: Project, context: VirtualFile): Boolean {
  if (!isVueServiceContext(project, context)) return false

  return when (getVueSettings(project).serviceType) {
    VueServiceSettings.AUTO, VueServiceSettings.VOLAR -> false
    VueServiceSettings.TS_SERVICE -> isTypeScriptServiceBefore5Context(project) // with TS 5+ project, nothing will be enabled
    VueServiceSettings.DISABLED -> false
  }
}

private fun isTypeScriptServiceBefore5Context(project: Project): Boolean {
  val path = getTypeScriptServiceDirectory(project)

  val packageJson = TypeScriptServerState.getPackageJsonFromServicePath(path)
  if (packageJson == null) return false // Nuxt doesn't have correct TS detection. Let's assume TS 5+
  val version = PackageJsonData.getOrCreate(packageJson).version ?: return true
  return version.major < 5
}

//</editor-fold>
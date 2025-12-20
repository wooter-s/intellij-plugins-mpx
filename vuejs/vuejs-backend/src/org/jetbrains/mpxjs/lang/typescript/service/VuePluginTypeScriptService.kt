// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.typescript.service

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceWidgetItem
import com.intellij.lang.typescript.compiler.languageService.frameworks.DownloadableTypeScriptServicePlugin
import com.intellij.lang.typescript.compiler.languageService.frameworks.PluggableTypeScriptService
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigurePluginRequest
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigurePluginRequestArguments
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.psi.PsiFile
import org.jetbrains.mpxjs.VuejsIcons
import org.jetbrains.mpxjs.lang.expr.VueJSLanguage
import org.jetbrains.mpxjs.lang.expr.VueTSLanguage
import org.jetbrains.mpxjs.lang.html.isVueFile
import org.jetbrains.mpxjs.options.VueConfigurable
import org.jetbrains.mpxjs.options.VueTSPluginVersion
import org.jetbrains.mpxjs.options.getVueSettings

private fun getVueTypeScriptPlugin(
  version: VueTSPluginVersion,
): DownloadableTypeScriptServicePlugin =
  DownloadableTypeScriptServicePlugin(
    shortLabel = "Vue",
    activationRule = VueTSPluginLoaderFactory.getActivationRule(version),
  )

class VuePluginTypeScriptService(
  project: Project,
) : PluggableTypeScriptService(
  project = project,
  servicePlugin = getVueTypeScriptPlugin(getVueSettings(project).tsPluginVersion)
) {
  override fun getInitialOpenCommands(): List<JSLanguageServiceSimpleCommand> {
    return listOf(createConfigureCommand()) + super.getInitialOpenCommands()
  }

  private fun createConfigureCommand(): JSLanguageServiceSimpleCommand {
    return ConfigurePluginRequest(
      ConfigurePluginRequestArguments(
        pluginName = "typescript-vue-plugin",
        configuration = emptyMap(),
      ))
  }

  override fun isAcceptableForHighlighting(file: PsiFile): Boolean {
    return super.isAcceptableForHighlighting(file)
           || file.isVueFile
  }

  override fun isAcceptableNonTsFile(
    project: Project,
    service: TypeScriptConfigService,
    virtualFile: VirtualFile,
  ): Boolean {
    return super.isAcceptableNonTsFile(project, service, virtualFile)
           || virtualFile.isVueFile
  }

  override fun supportsInjectedFile(file: PsiFile): Boolean {
    return file.language is VueJSLanguage
           || file.language is VueTSLanguage
  }

  override fun createWidgetItem(currentFile: VirtualFile?): LanguageServiceWidgetItem {
    return TypeScriptServiceWidgetItem(
      service = this,
      currentFile = currentFile,
      itemIcon = VuejsIcons.Vue,
      statusBarIcon = VuejsIcons.Vue,
      settingsPageClass = VueConfigurable::class.java,
    )
  }

  override fun getProcessName(): String =
    "Vue + TypeScript"

  override fun isTypeEvaluationEnabled(): Boolean =
    false
}
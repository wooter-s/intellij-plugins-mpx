// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.typescript.service.volar

import com.google.gson.JsonElement
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetElementTypeRequestArgs
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetSymbolTypeRequestArgs
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetTypePropertiesRequestArgs
import com.intellij.lang.typescript.lsp.BaseLspTypeScriptService
import com.intellij.lang.typescript.lsp.JSFrameworkLsp4jServer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.mpxjs.VueBundle
import org.jetbrains.mpxjs.lang.expr.VueJSLanguage
import org.jetbrains.mpxjs.lang.expr.VueTSLanguage
import org.jetbrains.mpxjs.lang.typescript.service.VueServiceSetActivationRule

class VolarTypeScriptService(project: Project) : BaseLspTypeScriptService(project, VolarSupportProvider::class.java) {
  override val name: String
    get() = VueBundle.message("vue.service.name")
  override val prefix: String
    get() = VueBundle.message("vue.service.prefix")

  override fun isAcceptable(file: VirtualFile) = VueServiceSetActivationRule.isLspServerEnabledAndAvailable(project, file)

  override suspend fun getIdeType(args: TypeScriptGetElementTypeRequestArgs): JsonElement? {
    val server = getServer() ?: return null
    return server.sendRequest { (it as JSFrameworkLsp4jServer).getElementType(args) }
  }

  override suspend fun getIdeSymbolType(args: TypeScriptGetSymbolTypeRequestArgs): JsonElement? {
    val server = getServer() ?: return null
    return server.sendRequest { (it as JSFrameworkLsp4jServer).getSymbolType(args) }
  }

  override suspend fun getIdeTypeProperties(args: TypeScriptGetTypePropertiesRequestArgs): JsonElement? {
    val server = getServer() ?: return null
    return server.sendRequest { (it as JSFrameworkLsp4jServer).getTypeProperties(args) }
  }

  override fun supportsTypeEvaluation(virtualFile: VirtualFile, element: PsiElement): Boolean {
    return virtualFile.extension == "vue" || super.supportsTypeEvaluation(virtualFile, element)
  }

  override fun supportsInjectedFile(file: PsiFile): Boolean {
    return file.language is VueJSLanguage || file.language is VueTSLanguage
  }

}
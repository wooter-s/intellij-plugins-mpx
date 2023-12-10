// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.typescript.service.volar

import com.intellij.lang.javascript.ecmascript6.TypeScriptAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.lsp.BaseLspTypeScriptService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.util.convertMarkupContentToHtml
import com.intellij.psi.PsiFile
import org.eclipse.lsp4j.MarkupContent
import org.jetbrains.mpxjs.VueBundle
import org.jetbrains.mpxjs.lang.typescript.service.isVolarEnabledAndAvailable
import org.jetbrains.mpxjs.lang.typescript.service.isVolarFileTypeAcceptable

class VolarTypeScriptService(project: Project) : BaseLspTypeScriptService(project, VolarSupportProvider::class.java) {
  override val name: String
    get() = VueBundle.message("vue.service.name")
  override val prefix: String
    get() = VueBundle.message("vue.service.prefix")

  override fun createQuickInfoResponse(markupContent: MarkupContent): TypeScriptQuickInfoResponse {
    return TypeScriptQuickInfoResponse().apply {
      val content = HtmlBuilder().appendRaw(convertMarkupContentToHtml(markupContent)).toString()
      val index = content.indexOf("<p>")
      val hrIndex = content.indexOf("<hr />")

      val firstPart: String
      val secondPart: String?
      if (hrIndex > -1) {
        // it has been proved that @vue/language-server@1.8.2 sometimes returns a duplicated symbol definition joined by a Markdown "---";
        // let's guard against this case by throwing away everything below it;
        // otherwise, our TypeScriptServiceQuickInfoParser can't deal with that, especially with possible generics inside.
        firstPart = content.substring(0, hrIndex)
        secondPart = null
      }
      else if (index > -1) {
        firstPart = content.substring(0, index)
        secondPart = content.substring(index)
      }
      else {
        firstPart = content
        secondPart = null
      }

      displayString = firstPart
        .removeSurrounding("<pre><code class=\"language-typescript\">", "</code></pre>")
        .trim()
        .let(StringUtil::unescapeXmlEntities)
      if (secondPart != null) {
        documentation = secondPart
      }
      // Vue LS omits "export" so we can't assign kindModifiers
    }
  }

  override fun canHighlight(file: PsiFile): Boolean {
    val provider = TypeScriptAnnotatorCheckerProvider.getCheckerProvider(file)
    if (provider !is TypeScriptLanguageServiceAnnotatorCheckerProvider) return false

    return isVolarFileTypeAcceptable(file.virtualFile ?: return false)
  }

  override fun isAcceptable(file: VirtualFile) = isVolarEnabledAndAvailable(project, file)

  override fun getServiceId(): String = "vue"
}
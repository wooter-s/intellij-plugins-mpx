// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.libraries.i18n

import com.intellij.lang.Language
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.xml.XmlTextImpl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsQueryConfigurator
import org.jetbrains.mpxjs.codeInsight.LANG_ATTRIBUTE_NAME
import org.jetbrains.mpxjs.lang.html.VueFileType
import org.jetbrains.mpxjs.web.VueFramework
import org.jetbrains.mpxjs.web.VueWebSymbolsQueryConfigurator

class VueI18NQueryConfigurator : WebSymbolsQueryConfigurator {

  override fun getScope(project: Project,
                        location: PsiElement?,
                        context: WebSymbolsContext,
                        allowResolve: Boolean): List<WebSymbolsScope> =
    if (context.framework == VueFramework.ID
        && location is HtmlTag
        && location.name == "i18n"
        && location.parentTag == null
        && location.containingFile?.virtualFile?.fileType == VueFileType.INSTANCE) {
      listOf(I18nTagInjectionKind(location))
    }
    else emptyList()

  private class I18nTagInjectionKind(private val tag: HtmlTag) : WebSymbolsScope {
    override fun equals(other: Any?): Boolean =
      other is I18nTagInjectionKind
      && other.tag == tag

    override fun hashCode(): Int =
      tag.hashCode()

    override fun createPointer(): Pointer<I18nTagInjectionKind> {
      val tag = this.tag.createSmartPointer()
      return Pointer {
        tag.dereference()?.let { I18nTagInjectionKind(it) }
      }
    }

    override fun getModificationCount(): Long = tag.containingFile.modificationStamp

    override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind,
                            params: WebSymbolsListSymbolsQueryParams,
                            scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> {
      if (qualifiedKind == VueWebSymbolsQueryConfigurator.VUE_TOP_LEVEL_ELEMENTS) {
        val language = tag.getAttributeValue(LANG_ATTRIBUTE_NAME)
                         ?.let { lang -> Language.getRegisteredLanguages().find { it.id.equals(lang, true) } }
                       ?: if (PsiTreeUtil.getChildOfType(tag, XmlTextImpl::class.java)
                           ?.chars?.find { !it.isWhitespace() }?.let { it == '{' || it == '[' } != false)
                         Language.findLanguageByID("JSON")
                       else
                         Language.findLanguageByID("yaml")
        return language?.id?.let { listOf(I18nTagExtension(it)) } ?: emptyList()
      }
      else return emptyList()
    }
  }

  private class I18nTagExtension(private val lang: String) : WebSymbol {

    override val name: String
      get() = "i18n"

    override val origin: WebSymbolOrigin =
      WebSymbolOrigin.create(VueFramework.ID, "vue-i18n")

    override val namespace: SymbolNamespace
      get() = WebSymbol.NAMESPACE_HTML

    override val kind: SymbolKind
      get() = WebSymbol.KIND_HTML_ELEMENTS

    override val extension: Boolean
      get() = true

    override val properties: Map<String, Any>
      get() = mapOf(Pair(WebSymbol.PROP_INJECT_LANGUAGE, lang))

    override fun createPointer(): Pointer<out WebSymbol> =
      Pointer.hardPointer(this)
  }

}
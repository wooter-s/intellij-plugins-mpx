// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web

import com.intellij.html.webSymbols.WebSymbolsHtmlQueryConfigurator
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.PsiSourcedWebSymbol
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolQualifiedName
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolsQueryResultsCustomizer
import com.intellij.webSymbols.query.WebSymbolsQueryResultsCustomizerFactory
import com.intellij.webSymbols.webTypes.WebTypesSymbol
import org.jetbrains.mpxjs.codeInsight.detectVueScriptLanguage
import org.jetbrains.mpxjs.codeInsight.tags.VueInsertHandler
import org.jetbrains.mpxjs.model.VueModelVisitor
import org.jetbrains.mpxjs.web.VueWebSymbolsQueryConfigurator.Companion.PROP_VUE_COMPOSITION_COMPONENT
import org.jetbrains.mpxjs.web.VueWebSymbolsQueryConfigurator.Companion.PROP_VUE_PROXIMITY
import org.jetbrains.mpxjs.web.symbols.VueWebTypesMergedSymbol

class VueWebSymbolsQueryResultsCustomizer(private val context: PsiElement) : WebSymbolsQueryResultsCustomizer {

  private val scriptLanguage by lazy(LazyThreadSafetyMode.PUBLICATION) {
    detectVueScriptLanguage(context.containingFile)
  }

  override fun createPointer(): Pointer<out WebSymbolsQueryResultsCustomizer> {
    val contextPtr = context.createSmartPointer()
    return Pointer {
      contextPtr.dereference()?.let { VueWebSymbolsQueryResultsCustomizer(it) }
    }
  }

  override fun apply(matches: List<WebSymbol>,
                     strict: Boolean,
                     qualifiedName: WebSymbolQualifiedName): List<WebSymbol> {
    if (qualifiedName.namespace != WebSymbol.NAMESPACE_HTML) return matches

    var result = matches
    if (qualifiedName.matches(VueWebSymbolsQueryConfigurator.VUE_COMPONENTS)) {
      if (result.size > 1) {
        val mergedSymbol = result.find { it is VueWebTypesMergedSymbol } as? VueWebTypesMergedSymbol
        if (mergedSymbol != null) {
          val mergedWebTypes = mergedSymbol.webTypesSymbols
          // The check can get very expensive with more web-types merged
          // Limit checks to some reasonable sizes. Usually it should be only one merged.
          if (mergedWebTypes.size < 5) {
            result = result.filter { it !is WebTypesSymbol || mergedWebTypes.none { merged -> merged.isEquivalentTo(it) } }
          }
        }
      }
      if (!strict) return result
    }
    else if (qualifiedName.matches(WebSymbol.HTML_ELEMENTS)) {
      val hasStandardHtmlSymbols = result.any { it is WebSymbolsHtmlQueryConfigurator.StandardHtmlSymbol }
      if (!hasStandardHtmlSymbols) return result
    }

    return result.filter { symbol ->
      symbol.properties[PROP_VUE_PROXIMITY] != VueModelVisitor.Proximity.OUT_OF_SCOPE ||
      symbol.properties[PROP_VUE_COMPOSITION_COMPONENT] == true
    }
  }

  override fun apply(item: WebSymbolCodeCompletionItem,
                     qualifiedKind: WebSymbolQualifiedKind): WebSymbolCodeCompletionItem {
    if (qualifiedKind == VueWebSymbolsQueryConfigurator.VUE_COMPONENTS) {
      val proximity = item.symbol?.properties?.get(PROP_VUE_PROXIMITY)
      val element = (item.symbol as? PsiSourcedWebSymbol)?.source
      if (proximity == VueModelVisitor.Proximity.OUT_OF_SCOPE && element != null) {
        val settings = JSApplicationSettings.getInstance()
        if ((scriptLanguage != null && "ts" == scriptLanguage)
            || (DialectDetector.isTypeScript(element)
                && !JSLibraryUtil.isProbableLibraryFile(element.containingFile.viewProvider.virtualFile))) {
          if (settings.hasTSImportCompletionEffective(element.project)) {
            return item.withInsertHandlerAdded(VueInsertHandler.INSTANCE, WebSymbol.Priority.LOWEST)
          }
        }
        else {
          if (settings.isUseJavaScriptAutoImport) {
            return item.withInsertHandlerAdded(VueInsertHandler.INSTANCE, WebSymbol.Priority.LOWEST)
          }
        }
      }
    }
    return item
  }

  override fun getModificationCount(): Long = 0

  override fun equals(other: Any?): Boolean =
    other is VueWebSymbolsQueryResultsCustomizer
    && other.context == context

  override fun hashCode(): Int =
    context.hashCode()

  class Provider : WebSymbolsQueryResultsCustomizerFactory {
    override fun create(location: PsiElement, context: WebSymbolsContext): WebSymbolsQueryResultsCustomizer? =
      if (context.framework == VueFramework.ID)
        VueWebSymbolsQueryResultsCustomizer(location)
      else null

  }

}
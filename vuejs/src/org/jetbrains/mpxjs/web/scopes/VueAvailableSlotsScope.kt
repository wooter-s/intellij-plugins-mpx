// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web.scopes

import com.intellij.model.Pointer
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.HTML_ATTRIBUTES
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import com.intellij.webSymbols.utils.match
import org.jetbrains.mpxjs.model.DEFAULT_SLOT_NAME
import org.jetbrains.mpxjs.model.getAvailableSlots
import org.jetbrains.mpxjs.model.getAvailableSlotsCompletions
import org.jetbrains.mpxjs.model.getMatchingAvailableSlots
import org.jetbrains.mpxjs.web.VUE_AVAILABLE_SLOTS
import org.jetbrains.mpxjs.web.VueFramework

class VueAvailableSlotsScope(private val tag: XmlTag) : WebSymbolsScope {

  override fun hashCode(): Int = tag.hashCode()

  override fun equals(other: Any?): Boolean =
    other is VueAvailableSlotsScope
    && other.tag == tag

  override fun getModificationCount(): Long = tag.containingFile.modificationStamp

  override fun getMatchingSymbols(qualifiedName: WebSymbolQualifiedName,
                                  params: WebSymbolsNameMatchQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbol> =
    when {
      !params.queryExecutor.allowResolve -> emptyList()
      qualifiedName.matches(HTML_ATTRIBUTES) ->
        DefaultSlotSymbol.match(qualifiedName.name, params, scope)
      qualifiedName.matches(VUE_AVAILABLE_SLOTS) ->
        getMatchingAvailableSlots(tag, qualifiedName.name, true)
      else -> emptyList()
    }

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind,
                          params: WebSymbolsListSymbolsQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    when {
      !params.queryExecutor.allowResolve -> emptyList()
      qualifiedKind == HTML_ATTRIBUTES ->
        listOf(DefaultSlotSymbol)
      qualifiedKind == VUE_AVAILABLE_SLOTS ->
        getAvailableSlots(tag, params.expandPatterns, true)
      else -> emptyList()
    }

  override fun getCodeCompletions(qualifiedName: WebSymbolQualifiedName,
                                  params: WebSymbolsCodeCompletionQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbolCodeCompletionItem> =
    if (qualifiedName.matches(VUE_AVAILABLE_SLOTS) && params.queryExecutor.allowResolve)
      getAvailableSlotsCompletions(tag, qualifiedName.name, params.position, true)
    else emptyList()

  override fun createPointer(): Pointer<VueAvailableSlotsScope> {
    val tag = this.tag.createSmartPointer()
    return Pointer {
      tag.dereference()?.let { VueAvailableSlotsScope(it) }
    }
  }

  object DefaultSlotSymbol : WebSymbol {
    override val namespace: SymbolNamespace
      get() = WebSymbol.NAMESPACE_HTML

    override val kind: SymbolKind
      get() = WebSymbol.KIND_HTML_ATTRIBUTES

    override val name: String
      get() = "v-slot"

    override val origin: WebSymbolOrigin = object : WebSymbolOrigin {
      override val framework: FrameworkId
        get() = VueFramework.ID
    }

    override val pattern: WebSymbolsPattern =
      WebSymbolsPatternFactory.createSingleSymbolReferencePattern(
        listOf(VUE_AVAILABLE_SLOTS.withName(DEFAULT_SLOT_NAME))
      )

    override fun createPointer(): Pointer<out WebSymbol> {
      return Pointer.hardPointer(this)
    }
  }
}
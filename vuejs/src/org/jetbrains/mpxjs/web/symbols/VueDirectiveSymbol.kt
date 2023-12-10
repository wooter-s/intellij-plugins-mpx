// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web.symbols

import com.intellij.model.Pointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolQualifiedName
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import org.jetbrains.mpxjs.codeInsight.fromAsset
import org.jetbrains.mpxjs.model.VueDirective
import org.jetbrains.mpxjs.model.VueModelVisitor
import org.jetbrains.mpxjs.web.VueWebSymbolsQueryConfigurator
import org.jetbrains.mpxjs.web.asWebSymbolPriority

open class VueDirectiveSymbol(name: String, directive: VueDirective, private val vueProximity: VueModelVisitor.Proximity) :
  VueScopeElementSymbol<VueDirective>(fromAsset(name), directive) {

  override val qualifiedKind: WebSymbolQualifiedKind
    get() = VueWebSymbolsQueryConfigurator.VUE_DIRECTIVES

  override val priority: WebSymbol.Priority
    get() = vueProximity.asWebSymbolPriority()

  override fun getMatchingSymbols(qualifiedName: WebSymbolQualifiedName,
                                  params: WebSymbolsNameMatchQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbol> =
    if (qualifiedName.matches(
        VueWebSymbolsQueryConfigurator.VUE_DIRECTIVE_ARGUMENT,
        VueWebSymbolsQueryConfigurator.VUE_DIRECTIVE_MODIFIERS,
      )) {
      listOf(VueAnySymbol(this.origin, WebSymbol.NAMESPACE_HTML, qualifiedName.kind, qualifiedName.name))
    }
    else emptyList()

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind,
                          params: WebSymbolsListSymbolsQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbol> =
    if (qualifiedKind == VueWebSymbolsQueryConfigurator.VUE_DIRECTIVE_ARGUMENT) {
      listOf(VueAnySymbol(this.origin, WebSymbol.NAMESPACE_HTML, qualifiedKind.kind, "Vue directive argument"))
    }
    else emptyList()

  override fun createPointer(): Pointer<VueDirectiveSymbol> {
    val component = item.createPointer()
    val name = this.name
    val vueProximity = this.vueProximity
    return Pointer {
      component.dereference()?.let { VueDirectiveSymbol(name, it, vueProximity) }
    }
  }
}
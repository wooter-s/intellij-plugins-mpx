// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web.symbols

import com.intellij.javascript.webSymbols.symbols.getJSPropertySymbols
import com.intellij.javascript.webSymbols.symbols.getMatchingJSPropertySymbols
import com.intellij.model.Pointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.JS_PROPERTIES
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import org.jetbrains.mpxjs.model.VueComponent
import org.jetbrains.mpxjs.model.VueProperty

abstract class VuePropertySymbol<T : VueProperty>(item: T, owner: VueComponent, origin: WebSymbolOrigin)
  : VueNamedWebSymbol<T>(item, owner, origin) {
  abstract override fun createPointer(): Pointer<out VuePropertySymbol<T>>

  override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == JS_PROPERTIES

  override fun getMatchingSymbols(qualifiedName: WebSymbolQualifiedName,
                                  params: WebSymbolsNameMatchQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbol> =
    getMatchingJSPropertySymbols(qualifiedName, params.queryExecutor.namesProvider)

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind,
                          params: WebSymbolsListSymbolsQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    getJSPropertySymbols(qualifiedKind)

}
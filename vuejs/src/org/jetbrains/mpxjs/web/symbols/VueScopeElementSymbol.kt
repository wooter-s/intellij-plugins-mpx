// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web.symbols

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.model.Pointer
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.refactoring.WebSymbolRenameTarget
import com.intellij.webSymbols.search.WebSymbolSearchTarget
import org.jetbrains.mpxjs.model.VueScopeElement

abstract class VueScopeElementSymbol<T : VueScopeElement>(name: String, item: T) :
  VueDocumentedItemSymbol<T>(name, item) {

  abstract override fun createPointer(): Pointer<out VueScopeElementSymbol<T>>

  override val origin: WebSymbolOrigin =
    VueScopeElementOrigin(item)

  override val searchTarget: WebSymbolSearchTarget?
    get() = WebSymbolSearchTarget.create(this)

  override val renameTarget: WebSymbolRenameTarget?
    get() = if (source is JSLiteralExpression)
      WebSymbolRenameTarget.create(this)
    else null
}
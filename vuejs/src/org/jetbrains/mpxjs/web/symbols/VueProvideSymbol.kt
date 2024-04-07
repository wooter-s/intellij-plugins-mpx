// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web.symbols

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.psi.PsiNamedElement
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.WebSymbolQualifiedKind
import org.jetbrains.mpxjs.model.VueContainer
import org.jetbrains.mpxjs.model.VueProvide
import org.jetbrains.mpxjs.web.VUE_PROVIDES

class VueProvideSymbol(
  private val provide: VueProvide,
  private val owner: VueContainer,
  override val origin: WebSymbolOrigin,
) : VueDocumentedItemSymbol<VueProvide>(provide.name, provide) {

  override val qualifiedKind: WebSymbolQualifiedKind
    get() = VUE_PROVIDES

  override val type: JSType?
    get() = item.jsType

  val injectionKey: PsiNamedElement?
    get() = provide.injectionKey

  override fun createPointer(): Pointer<VueProvideSymbol> = object : Pointer<VueProvideSymbol> {
    private val name = this@VueProvideSymbol.name
    private val origin = this@VueProvideSymbol.origin
    private val pointer = this@VueProvideSymbol.owner.createPointer()

    override fun dereference(): VueProvideSymbol? =
      pointer.dereference()?.asSafely<VueContainer>()?.let { container ->
        container.provides.find { it.name == name }?.let { provide ->
          VueProvideSymbol(provide, container, origin)
        }
      }
  }
}
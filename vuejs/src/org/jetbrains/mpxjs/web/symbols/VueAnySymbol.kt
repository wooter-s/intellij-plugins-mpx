// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web.symbols

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer

class VueAnySymbol(
  override val origin: PolySymbolOrigin,
  override val qualifiedKind: PolySymbolQualifiedKind,
  override val name: String,
  val type: JSType? = null,
) : PolySymbolWithPattern {

  override val pattern: PolySymbolPattern
    get() = PolySymbolPatternFactory.createRegExMatch(".*", false)

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PolySymbol.PROP_HIDE_FROM_COMPLETION -> true as? T
      PolySymbol.PROP_DOC_HIDE_PATTERN -> true as? T
      PROP_JS_TYPE -> type as? T
      else -> null
    }

  override fun createPointer(): Pointer<VueAnySymbol> =
    Pointer.hardPointer(this)
}
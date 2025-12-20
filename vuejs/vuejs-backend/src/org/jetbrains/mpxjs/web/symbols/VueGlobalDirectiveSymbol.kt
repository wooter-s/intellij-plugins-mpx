// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web.symbols

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolQualifiedKind
import org.jetbrains.mpxjs.model.VueModelVisitor
import org.jetbrains.mpxjs.model.typed.VueTypedDirective
import org.jetbrains.mpxjs.web.VUE_GLOBAL_DIRECTIVES

class VueGlobalDirectiveSymbol(
  directive: VueTypedDirective,
  override val vueProximity: VueModelVisitor.Proximity,
) : VueDirectiveSymbolBase<VueTypedDirective>(
  name = directive.defaultName,
  directive = directive,
) {

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = VUE_GLOBAL_DIRECTIVES

  override fun createPointer(): Pointer<VueGlobalDirectiveSymbol> {
    val directive = item.createPointer()
    val vueProximity = this.vueProximity
    return Pointer {
      directive.dereference()?.let { VueGlobalDirectiveSymbol(it, vueProximity) }
    }
  }
}
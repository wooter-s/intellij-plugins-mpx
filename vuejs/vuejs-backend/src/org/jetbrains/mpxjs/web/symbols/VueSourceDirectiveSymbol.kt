// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web.symbols

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolQualifiedKind
import org.jetbrains.mpxjs.codeInsight.fromAsset
import org.jetbrains.mpxjs.model.VueModelVisitor
import org.jetbrains.mpxjs.model.source.VueSourceDirective
import org.jetbrains.mpxjs.web.VUE_DIRECTIVES

class VueSourceDirectiveSymbol(
  directive: VueSourceDirective,
  override val vueProximity: VueModelVisitor.Proximity,
) : VueDirectiveSymbolBase<VueSourceDirective>(
  name = fromAsset(directive.defaultName),
  directive = directive,
) {

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = VUE_DIRECTIVES

  override fun createPointer(): Pointer<VueSourceDirectiveSymbol> {
    val directive = item.createPointer()
    val vueProximity = this.vueProximity
    return Pointer {
      directive.dereference()?.let { VueSourceDirectiveSymbol(it, vueProximity) }
    }
  }
}
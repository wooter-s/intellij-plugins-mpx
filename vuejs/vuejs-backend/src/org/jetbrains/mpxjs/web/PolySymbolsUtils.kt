// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.web

import com.intellij.polySymbols.html.elements.HtmlElementSymbolDescriptor
import com.intellij.polySymbols.js.types.TypeScriptSymbolTypeSupport
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import org.jetbrains.mpxjs.VuejsIcons
import org.jetbrains.mpxjs.codeInsight.toAsset
import org.jetbrains.mpxjs.model.*
import org.jetbrains.mpxjs.model.source.VueScriptSetupLocalDirective
import org.jetbrains.mpxjs.model.source.VueSourceDirective
import org.jetbrains.mpxjs.model.typed.VueTypedDirective
import org.jetbrains.mpxjs.web.symbols.VueComponentSymbol
import org.jetbrains.mpxjs.web.symbols.VueDirectiveSymbol
import org.jetbrains.mpxjs.web.symbols.VueGlobalDirectiveSymbol
import org.jetbrains.mpxjs.web.symbols.VueScriptSetupLocalDirectiveSymbol
import org.jetbrains.mpxjs.web.symbols.VueSourceDirectiveSymbol

fun HtmlElementSymbolDescriptor.getModel(): VueModelDirectiveProperties =
  runListSymbolsQuery(VUE_MODEL, true).firstOrNull()
    ?.let {
      VueModelDirectiveProperties(prop = it[PROP_VUE_MODEL_PROP],
                                  event = it[PROP_VUE_MODEL_EVENT])
    }
  ?: VueModelDirectiveProperties()

fun VueScopeElement.asPolySymbol(name: String, forcedProximity: VueModelVisitor.Proximity): PolySymbol? =
  when (this) {
    is VueComponent -> VueComponentSymbol(toAsset(name, true), this, forcedProximity)
    is VueTypedDirective -> VueGlobalDirectiveSymbol(this, forcedProximity)
    is VueSourceDirective -> VueSourceDirectiveSymbol(this, forcedProximity)
    is VueScriptSetupLocalDirective -> VueScriptSetupLocalDirectiveSymbol(this, forcedProximity)
    is VueDirective -> VueDirectiveSymbol(name, this, forcedProximity)
    else -> null
  }

fun VueModelVisitor.Proximity.asPolySymbolPriority(): PolySymbol.Priority =
  when (this) {
    VueModelVisitor.Proximity.LOCAL -> PolySymbol.Priority.HIGHEST
    VueModelVisitor.Proximity.APP -> PolySymbol.Priority.HIGH
    VueModelVisitor.Proximity.LIBRARY, VueModelVisitor.Proximity.GLOBAL -> PolySymbol.Priority.NORMAL
    VueModelVisitor.Proximity.OUT_OF_SCOPE -> PolySymbol.Priority.LOW
  }

val vueEmptyOrigin: PolySymbolOrigin = PolySymbolOrigin.create(
  VueFramework.ID,
  library = "vue",
  defaultIcon = VuejsIcons.Vue,
  typeSupport = TypeScriptSymbolTypeSupport())

internal fun isVueComponentQuery(name: String): Boolean {
  return name.getOrNull(0)?.isUpperCase() == true || name.contains('-') || name == "slot"
}
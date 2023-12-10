// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.web

import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.webSymbols.WebSymbol
import org.jetbrains.mpxjs.codeInsight.toAsset
import org.jetbrains.mpxjs.model.*
import org.jetbrains.mpxjs.model.source.VueScriptSetupLocalDirective
import org.jetbrains.mpxjs.web.VueWebSymbolsQueryConfigurator.Companion.PROP_VUE_MODEL_EVENT
import org.jetbrains.mpxjs.web.VueWebSymbolsQueryConfigurator.Companion.PROP_VUE_MODEL_PROP
import org.jetbrains.mpxjs.web.VueWebSymbolsQueryConfigurator.Companion.VUE_MODEL
import org.jetbrains.mpxjs.web.symbols.VueComponentSymbol
import org.jetbrains.mpxjs.web.symbols.VueDirectiveSymbol
import org.jetbrains.mpxjs.web.symbols.VueScriptSetupLocalDirectiveSymbol

fun WebSymbolElementDescriptor.getModel(): VueModelDirectiveProperties =
  runListSymbolsQuery(VUE_MODEL, true).firstOrNull()
    ?.let {
      VueModelDirectiveProperties(prop = it.properties[PROP_VUE_MODEL_PROP] as? String,
                                  event = it.properties[PROP_VUE_MODEL_EVENT] as? String)
    }
  ?: VueModelDirectiveProperties()

fun VueScopeElement.asWebSymbol(name: String, forcedProximity: VueModelVisitor.Proximity): WebSymbol? =
  when (this) {
    is VueComponent -> VueComponentSymbol(toAsset(name, true), this, forcedProximity)
    is VueScriptSetupLocalDirective -> VueScriptSetupLocalDirectiveSymbol(this, forcedProximity)
    is VueDirective -> VueDirectiveSymbol(name, this, forcedProximity)
    else -> null
  }

fun VueModelVisitor.Proximity.asWebSymbolPriority(): WebSymbol.Priority =
  when (this) {
    VueModelVisitor.Proximity.LOCAL -> WebSymbol.Priority.HIGHEST
    VueModelVisitor.Proximity.APP -> WebSymbol.Priority.HIGH
    VueModelVisitor.Proximity.PLUGIN, VueModelVisitor.Proximity.GLOBAL -> WebSymbol.Priority.NORMAL
    VueModelVisitor.Proximity.OUT_OF_SCOPE -> WebSymbol.Priority.LOW
  }
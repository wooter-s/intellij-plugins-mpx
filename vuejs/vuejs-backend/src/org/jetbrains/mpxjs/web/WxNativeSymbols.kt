// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web

import com.intellij.polySymbols.CompositePolySymbol
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.webTypes.WebTypesSymbol
import org.jetbrains.mpxjs.web.symbols.VueWebTypesMergedSymbol
import java.util.Collections
import java.util.IdentityHashMap

const val WX_NATIVE_LIBRARY: String = "wx"

/**
 * Checks the Web-Types origin instead of the active framework because wx symbols
 * are contributed to the MPX context without claiming to be part of that framework.
 */
fun PolySymbol.isWxNativeSymbol(): Boolean =
  isWxNativeSymbol(Collections.newSetFromMap(IdentityHashMap()))

private fun PolySymbol.isWxNativeSymbol(visited: MutableSet<PolySymbol>): Boolean {
  if (!visited.add(this)) return false

  if (this is WebTypesSymbol && origin.library == WX_NATIVE_LIBRARY) return true

  val nestedSymbols = when (this) {
    is VueWebTypesMergedSymbol -> webTypesSymbols.asSequence()
    is CompositePolySymbol -> nameSegments.asSequence().flatMap { it.symbols.asSequence() }
    else -> emptySequence()
  }
  return nestedSymbols.any { it.isWxNativeSymbol(visited) }
}

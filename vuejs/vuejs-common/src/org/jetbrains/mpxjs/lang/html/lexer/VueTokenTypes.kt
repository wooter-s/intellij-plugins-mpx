// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.html.lexer

interface VueTokenTypes {
  companion object {
    @JvmField
    val INTERPOLATION_START: VueTokenType = VueTokenType("MPX:INTERPOLATION_START")

    @JvmField
    val INTERPOLATION_END: VueTokenType = VueTokenType("MPX:INTERPOLATION_END")

    @JvmField
    val INTERPOLATION_EXPR: VueTokenType = VueTokenType("MPX:INTERPOLATION_EXPR")
  }
}

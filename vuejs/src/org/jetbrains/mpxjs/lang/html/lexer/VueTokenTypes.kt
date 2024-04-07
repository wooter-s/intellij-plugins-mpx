// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.html.lexer

import com.intellij.psi.xml.XmlTokenType

interface VueTokenTypes : XmlTokenType {
  companion object {
    @JvmField
    val INTERPOLATION_START = VueTokenType("MPX:INTERPOLATION_START")

    @JvmField
    val INTERPOLATION_END = VueTokenType("MPX:INTERPOLATION_END")

    @JvmField
    val INTERPOLATION_EXPR = VueTokenType("MPX:INTERPOLATION_EXPR")
  }
}

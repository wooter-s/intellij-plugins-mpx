// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.html

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.Lexer
import org.jetbrains.mpxjs.lang.LangMode
import org.jetbrains.mpxjs.lang.html.lexer.VueLexer

open class VueHighlightingLexerTest : VueLexerTest() {
  override fun createLexer(): Lexer = VueLexer(JSLanguageLevel.ES6, null, interpolationConfig, false, true, LangMode.DEFAULT)
  override fun getDirPath() = "html/highlightingLexer"
}

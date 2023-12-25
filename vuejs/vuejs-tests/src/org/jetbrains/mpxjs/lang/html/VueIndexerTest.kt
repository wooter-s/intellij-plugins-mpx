// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.html

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.Lexer
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer
import org.jetbrains.mpxjs.lang.LangMode
import org.jetbrains.mpxjs.lang.html.index.VueFilterLexer
import org.jetbrains.mpxjs.lang.html.lexer.VueLexer

class VueIndexerTest : VueHighlightingLexerTest() {
  override fun createLexer(): Lexer {
    return VueFilterLexer(OccurrenceConsumer(null, false),
                          VueLexer(JSLanguageLevel.ES6, null, interpolationConfig, false, true, LangMode.DEFAULT))
  }
}

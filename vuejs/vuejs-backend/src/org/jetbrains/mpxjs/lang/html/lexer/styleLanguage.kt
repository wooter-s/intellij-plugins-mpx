// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.html.lexer

import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage

fun styleLanguage(styleLang: String?): Language {
  val cssLanguage = CSSLanguage.INSTANCE
  // PostCSS implementation classes belong to an internal platform module.
  val defaultLanguage = Language.findLanguageByID("PostCSS") ?: cssLanguage
  if (styleLang == null) return defaultLanguage
  if (styleLang.equals("text/css", ignoreCase = true)) return cssLanguage
  return cssLanguage.dialects.firstOrNull { dialect ->
    dialect.id.equals(styleLang, ignoreCase = true)
    || dialect.mimeTypes.any { it.equals(styleLang, ignoreCase = true) }
  } ?: defaultLanguage
}

// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.html.lexer

import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage

private const val CSS_MIME_TYPE = "text/css"
private const val POST_CSS_LANGUAGE_ID = "PostCSS"

fun styleLanguage(styleLang: String?): Language {
  val cssLanguage = CSSLanguage.INSTANCE
  val postCssLanguage = Language.findLanguageByID(POST_CSS_LANGUAGE_ID) ?: cssLanguage
  if (styleLang == null) return postCssLanguage
  if (styleLang.equals(CSS_MIME_TYPE, ignoreCase = true)) return cssLanguage

  return cssLanguage.dialects.firstOrNull { language ->
    language.id.equals(styleLang, ignoreCase = true)
    || language.mimeTypes.any { it.equals(styleLang, ignoreCase = true) }
  } ?: postCssLanguage
}

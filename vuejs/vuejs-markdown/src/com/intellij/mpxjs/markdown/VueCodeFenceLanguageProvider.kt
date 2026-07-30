// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.mpxjs.markdown

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import org.intellij.plugins.markdown.injection.CodeFenceLanguageProvider

private const val MPX_LANGUAGE_ID = "Mpx"
private const val MPX_INFO_STRING = "mpx"

class VueCodeFenceLanguageProvider : CodeFenceLanguageProvider {
  override fun getLanguageByInfoString(infoString: String): Language? {
    // Avoid touching VueLanguage directly to prevent duplicate language registration across classloaders.
    val language = Language.findLanguageByID(MPX_LANGUAGE_ID) ?: return null
    val matches = infoString.equals(language.id, true) ||
      infoString.equals(language.displayName, true) ||
      infoString.equals(MPX_INFO_STRING, true)

    return if (matches) language else null
  }

  override fun getInfoStringForLanguage(language: Language, context: PsiElement?): String? {
    return if (language.id.equals(MPX_LANGUAGE_ID, true)) MPX_INFO_STRING else null
  }

  override fun getCompletionVariantsForInfoString(parameters: CompletionParameters): List<LookupElement> = emptyList()
}

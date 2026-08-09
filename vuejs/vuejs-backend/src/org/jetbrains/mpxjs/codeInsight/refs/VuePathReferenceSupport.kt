// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.codeInsight.refs

import com.intellij.lang.javascript.frameworks.JSXmlAttributePathUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.paths.GlobalPathReferenceProvider
import com.intellij.openapi.paths.PathReferenceManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.XmlAttributeValuePattern
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.xml.util.HtmlUtil
import java.util.Locale

// Keep route-link references available without linking the JetBrains-only JavaScript ultimate module.
internal fun createVuePathReferenceProvider(): PsiReferenceProvider = object : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    val text = StringUtil.unquoteString(element.text)
    if (HtmlUtil.hasHtmlPrefix(text)
        || StringUtil.startsWithIgnoreCase(text, "javascript:")
        || GlobalPathReferenceProvider.startsWithAllowedPrefix(text)
        || !(text.startsWith("/") || text.startsWith("`/"))
    ) {
      return PsiReference.EMPTY_ARRAY
    }

    return PathReferenceManager.getInstance().createReferences(
      element,
      false,
      false,
      true,
      FileType.EMPTY_ARRAY,
    )
  }
}

internal fun createVuePathAttributePattern(allowHtmlNames: Boolean): XmlAttributeValuePattern =
  XmlPatterns.xmlAttributeValue("href", "to")
    .withSuperParent(
      2,
      XmlPatterns.xmlTag().and(FilterPattern(VuePathContainedTagFilter(allowHtmlNames))),
    )

internal fun createVuePathContainedTagFilter(allowHtmlNames: Boolean): ElementFilter =
  VuePathContainedTagFilter(allowHtmlNames)

private class VuePathContainedTagFilter(
  private val allowHtmlNames: Boolean,
) : ElementFilter {

  override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
    val tag = element as? XmlTag ?: return false
    val name = tag.name
    if (!allowHtmlNames && (name == "a" || name == "link")) return false
    if (!name.lowercase(Locale.ROOT).endsWith("link")) return false
    return JSXmlAttributePathUtil.additionalRoots(tag).isNotEmpty()
  }

  override fun isClassAcceptable(hintClass: Class<*>?): Boolean =
    hintClass == null || XmlTag::class.java.isAssignableFrom(hintClass)
}

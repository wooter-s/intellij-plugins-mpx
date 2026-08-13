// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.html.HTML_ELEMENTS
import com.intellij.polySymbols.html.elements.HtmlElementSymbolDescriptor
import com.intellij.polySymbols.query.PolySymbolQueryExecutorFactory
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import org.jetbrains.mpxjs.VueBundle
import org.jetbrains.mpxjs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.mpxjs.lang.html.isVueFile
import org.jetbrains.mpxjs.web.isWxNativeSymbol

/**
 * Inspection to detect unrecognized attributes on WeChat Mini-Program components.
 *
 * This inspection only checks elements resolved from the wx Web Types origin and ignores
 * standard HTML tags and custom components. It also skips wx: directives and
 * common attributes.
 */
class MpxUnrecognizedAttributeInspection : LocalInspectionTool() {

  companion object {
    // Common attributes that are valid for all components
    private val COMMON_ATTRIBUTES = setOf(
      "class", "style", "id", "hidden", "slot"
    )

    // wx: directive prefixes that should be skipped
    private val WX_DIRECTIVE_PREFIXES = listOf(
      "wx:", "bind:", "catch:", "capture-bind:", "capture-catch:",
      "mut-bind:", "mark:", "model:", "bindtap", "catchtap", "bind",
      "catch", "capture-bind", "capture-catch", "mut-bind"
    )

    // Event binding prefixes (merged syntax like bindtap, catchtap, etc.)
    private val EVENT_BINDING_PREFIXES = listOf(
      "bind", "catch", "capture-bind", "capture-catch", "mut-bind"
    )

  }

  override fun buildVisitor(
    holder: ProblemsHolder,
    isOnTheFly: Boolean,
    session: LocalInspectionToolSession
  ): PsiElementVisitor {
    return object : XmlElementVisitor() {
      override fun visitXmlAttribute(attribute: XmlAttribute) {
        val tag = attribute.parent
        val attrName = attribute.name

        // Skip if attribute name is empty
        if (attrName.isBlank()) return

        // Skip wx: directives and event bindings
        if (isWxDirectiveOrEventBinding(attrName)) return

        // Skip common attributes
        if (isCommonAttribute(attrName)) return

        // Skip data-* attributes
        if (attrName.startsWith("data-")) return

        // Check if this is a WeChat Mini-Program component
        if (!isWeChatMiniProgramComponent(tag)) return

        // Get valid attributes for this component
        val validAttributes = getValidAttributes(tag)

        // If we couldn't get valid attributes, don't report (might be a custom component)
        if (validAttributes.isEmpty()) return

        // Check if the attribute is valid
        if (!isAttributeValid(attrName, validAttributes)) {
          holder.registerProblem(
            attribute.nameElement ?: attribute,
            VueBundle.message("mpx.inspection.message.unrecognized.attribute", attrName, tag.name),
            ProblemHighlightType.WARNING
          )
        }
      }
    }
  }

  /**
   * Check if the attribute name is a wx: directive or event binding
   */
  private fun isWxDirectiveOrEventBinding(attrName: String): Boolean {
    // Check for wx: directive prefixes
    for (prefix in WX_DIRECTIVE_PREFIXES) {
      if (attrName.startsWith(prefix)) {
        return true
      }
    }

    // Check for event bindings (bindtap, catchtap, etc.)
    // These are handled by existing directive inspection
    val parsed = VueAttributeNameParser.parse(attrName, null)
    if (parsed is VueAttributeNameParser.VueDirectiveInfo) {
      return true
    }

    return false
  }

  /**
   * Check if the attribute is a common attribute valid for all components
   */
  private fun isCommonAttribute(attrName: String): Boolean {
    return attrName in COMMON_ATTRIBUTES
  }

  /**
   * Check if the tag is a WeChat Mini-Program component
   */
  private fun isWeChatMiniProgramComponent(tag: XmlTag): Boolean {
    val descriptor = tag.descriptor
    return descriptor is HtmlElementSymbolDescriptor
           && descriptor.symbol.isWxNativeSymbol()
  }

  /**
   * Get valid attributes for a tag from web-types
   */
  private fun getValidAttributes(tag: XmlTag): Set<String> {
    val validAttrs = mutableSetOf<String>()

    try {
      val executor = PolySymbolQueryExecutorFactory.create(tag)

      // Query for all available attributes for this element
      val attributes = executor
        .listSymbolsQuery(HTML_ATTRIBUTES, expandPatterns = true)
        .exclude(PolySymbolModifier.ABSTRACT)
        .additionalScope(
          executor.nameMatchQuery(HTML_ELEMENTS, tag.name)
            .exclude(PolySymbolModifier.ABSTRACT)
            .run()
            .flatMap { it.queryScope }
        )
        .run()

      for (attr in attributes) {
        validAttrs.add(attr.name)
      }
    } catch (e: Exception) {
      // If we can't query, return empty set (won't report problems)
    }

    return validAttrs
  }

  /**
   * Check if an attribute name is valid for the component
   */
  private fun isAttributeValid(attrName: String, validAttributes: Set<String>): Boolean {
    // Direct match
    if (attrName in validAttributes) return true

    // Check without special characters (for attributes like aria-*)
    val normalizedName = attrName.replace("-", "")
    for (validAttr in validAttributes) {
      if (validAttr.replace("-", "") == normalizedName) {
        return true
      }
    }

    return false
  }

  override fun isAvailableForFile(file: PsiFile): Boolean = file.isVueFile
}

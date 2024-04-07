// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.model

import com.intellij.html.webSymbols.attributes.WebSymbolAttributeDescriptor
import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.javascript.webSymbols.jsType
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.StubSafe
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.lang.javascript.psi.types.JSNamedType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.xml.util.HtmlUtil.TEMPLATE_TAG_NAME
import org.jetbrains.mpxjs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.mpxjs.types.asCompleteType

const val DEFAULT_SLOT_NAME = "default"
const val SLOT_NAME_ATTRIBUTE = "name"

const val DEPRECATED_SLOT_ATTRIBUTE = "slot"

fun getMatchingAvailableSlots(tag: XmlTag, name: String, newApi: Boolean): List<WebSymbol> =
  processSlots(tag, newApi) {
    runNameMatchQuery(WebSymbol.NAMESPACE_HTML, WebSymbol.KIND_HTML_SLOTS, name)
  }

fun getAvailableSlots(tag: XmlTag, expandPatterns: Boolean, newApi: Boolean): List<WebSymbol> =
  processSlots(tag, newApi) {
    runListSymbolsQuery(WebSymbol.HTML_SLOTS, expandPatterns)
  }

fun getAvailableSlotsCompletions(tag: XmlTag, name: String, position: Int, newApi: Boolean): List<WebSymbolCodeCompletionItem> =
  processSlots(tag, newApi) {
    runCodeCompletionQuery(WebSymbol.HTML_SLOTS, name, position)
  }

private fun <T> processSlots(tag: XmlTag,
                             newApi: Boolean,
                             process: WebSymbolElementDescriptor.() -> List<T>): List<T> =
  if (!newApi || tag.name == TEMPLATE_TAG_NAME)
    (tag.parentTag?.descriptor as? WebSymbolElementDescriptor)?.process() ?: emptyList()
  else
    (tag.descriptor as? WebSymbolElementDescriptor)?.process() ?: emptyList()


fun getSlotTypeFromContext(context: PsiElement): JSType? =
  context.parentOfType<XmlAttribute>()
    ?.takeIf { attribute ->
      VueAttributeNameParser.parse(attribute.name, attribute.parent).let {
        it is VueAttributeNameParser.VueDirectiveInfo
        && it.directiveKind == VueAttributeNameParser.VueDirectiveKind.SLOT
      }
    }
    ?.descriptor
    ?.asSafely<WebSymbolAttributeDescriptor>()
    ?.symbol
    ?.jsType
    ?.asCompleteType()

/**
 * Get type from an explicitly typed slots property when using Options API.
 *
 * ```
 * slots: Object as SlotsType<{
 *   default: { foo: string; bar: number },
 *   item: { data: number }
 * }>
 * ```
 */
@StubSafe
fun getSlotsTypeFromTypedProperty(property: JSProperty?): JSType? =
  property
    ?.initializerOrStub?.asSafely<TypeScriptAsExpression>()
    ?.type?.jsType?.asSafely<JSGenericTypeImpl>()
    ?.takeIf { JSNamedType.isNamedTypeWithName(it.type, "SlotsType") }
    ?.arguments
    ?.firstOrNull()

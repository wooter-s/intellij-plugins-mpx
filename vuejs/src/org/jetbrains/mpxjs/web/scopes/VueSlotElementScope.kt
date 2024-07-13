// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web.scopes

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.util.stubSafeAttributes
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.HTML_ATTRIBUTES
import com.intellij.webSymbols.WebSymbol.Companion.HTML_SLOTS
import com.intellij.webSymbols.WebSymbol.Companion.JS_PROPERTIES
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.html.WebSymbolHtmlAttributeValue
import com.intellij.webSymbols.html.WebSymbolHtmlAttributeValue.Kind.EXPRESSION
import com.intellij.webSymbols.html.WebSymbolHtmlAttributeValue.Type.OF_MATCH
import com.intellij.webSymbols.patterns.ComplexPatternOptions
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createComplexPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createSymbolReferencePlaceholder
import com.intellij.webSymbols.patterns.WebSymbolsPatternReferenceResolver
import com.intellij.webSymbols.patterns.WebSymbolsPatternReferenceResolver.Reference
import com.intellij.webSymbols.query.WebSymbolNameConversionRules
import com.intellij.xml.util.HtmlUtil.SLOT_TAG_NAME
import org.jetbrains.mpxjs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.mpxjs.codeInsight.findJSExpression
import org.jetbrains.mpxjs.codeInsight.fromAsset
import org.jetbrains.mpxjs.codeInsight.toAsset
import org.jetbrains.mpxjs.model.DEFAULT_SLOT_NAME
import org.jetbrains.mpxjs.model.SLOT_NAME_ATTRIBUTE
import org.jetbrains.mpxjs.model.VueModelManager
import org.jetbrains.mpxjs.model.VueModelVisitor
import org.jetbrains.mpxjs.web.VUE_COMPONENTS
import org.jetbrains.mpxjs.web.VUE_SPECIAL_PROPERTIES
import org.jetbrains.mpxjs.web.VueFramework
import org.jetbrains.mpxjs.web.asWebSymbol

private const val SLOT_LOCAL_COMPONENT = "\$local"

class VueSlotElementScope(tag: XmlTag)
  : WebSymbolsScopeWithCache<XmlTag, Unit>(VueFramework.ID, tag.project, tag, Unit) {

  override fun provides(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == HTML_ATTRIBUTES
    || qualifiedKind == VUE_COMPONENTS

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    VueModelManager.findEnclosingContainer(dataHolder)
      .asWebSymbol(SLOT_LOCAL_COMPONENT, VueModelVisitor.Proximity.LOCAL)
      ?.let(consumer)

    consumer(VueSlotPropertiesSymbol(getSlotName()))

    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  private fun getSlotName(): String? {
    for (attr in dataHolder.stubSafeAttributes) {
      val info = VueAttributeNameParser.parse(attr.name, SLOT_TAG_NAME)

      if (info.kind == VueAttributeNameParser.VueAttributeKind.SLOT_NAME) {
        return attr.value
      }
      else if ((info as? VueAttributeNameParser.VueDirectiveInfo)?.directiveKind == VueAttributeNameParser.VueDirectiveKind.BIND
               && info.arguments == SLOT_NAME_ATTRIBUTE) {
        return attr.valueElement
          ?.findJSExpression<JSExpression>()
          ?.let { JSResolveUtil.getExpressionJSType(it)?.substitute() }
          ?.asSafely<JSStringLiteralTypeImpl>()
          ?.literal
      }
    }

    return DEFAULT_SLOT_NAME
  }

  override fun createPointer(): Pointer<VueSlotElementScope> {
    val componentPointer = dataHolder.createSmartPointer()
    return Pointer {
      componentPointer.dereference()?.let { VueSlotElementScope(it) }
    }
  }

  private class VueSlotPropertiesSymbol(slotName: String?) : WebSymbol {

    override val namespace: SymbolNamespace
      get() = NAMESPACE_HTML

    override val kind: SymbolKind
      get() = WebSymbol.KIND_HTML_ATTRIBUTES

    override val name: String
      get() = "Vue Slot Properties"

    override val attributeValue: WebSymbolHtmlAttributeValue
      get() = WebSymbolHtmlAttributeValue.create(kind = EXPRESSION, type = OF_MATCH)

    override val pattern: WebSymbolsPattern =
      createComplexPattern(
        ComplexPatternOptions(symbolsResolver = WebSymbolsPatternReferenceResolver(
          if (slotName != null)
            Reference(
              location = listOf(
                VUE_COMPONENTS.withName(SLOT_LOCAL_COMPONENT),
                HTML_SLOTS.withName(slotName),
              ),
              qualifiedKind = JS_PROPERTIES,
              nameConversionRules = listOf(
                WebSymbolNameConversionRules.create(JS_PROPERTIES) {
                  listOf(fromAsset(it), toAsset(it))
                }
              )
            )
          else
            Reference(qualifiedKind = VUE_SPECIAL_PROPERTIES)
        )), false,
        createSymbolReferencePlaceholder()
      )

    override val origin: WebSymbolOrigin = object : WebSymbolOrigin {
      override val framework: FrameworkId
        get() = VueFramework.ID
    }

    override fun createPointer(): Pointer<out WebSymbol> =
      Pointer.hardPointer(this)
  }

}
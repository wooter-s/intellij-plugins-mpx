// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web.scopes

import com.intellij.model.Pointer
import com.intellij.polySymbols.*
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.html.HTML_ELEMENTS
import com.intellij.polySymbols.query.*
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.polySymbols.utils.ReferencingPolySymbol
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import org.jetbrains.mpxjs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.mpxjs.codeInsight.template.VueTemplateScopesResolver
import org.jetbrains.mpxjs.web.VUE_BINDING_SHORTHANDS
import org.jetbrains.mpxjs.web.VueFramework

class VueBindingShorthandScope(attribute: XmlAttribute)
  : PolySymbolScopeWithCache<XmlAttribute, Unit>(null, attribute.project, attribute, Unit) {

  override fun provides(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    qualifiedKind == VUE_BINDING_SHORTHANDS

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)

    val tag = dataHolder.context as? XmlTag ?: return
    VueAttributeNameParser.parse(dataHolder.name, tag)
      .asSafely<VueAttributeNameParser.VueDirectiveInfo>()
      ?.takeIf { it.directiveKind == VueAttributeNameParser.VueDirectiveKind.BIND } ?: return

    // Create a referencing symbol that points to template scope properties
    consumer(VueBindingShorthandSymbol)
  }

  override fun createPointer(): Pointer<VueBindingShorthandScope> {
    val pointer = dataHolder.createSmartPointer()
    return Pointer {
      pointer.dereference()?.let { VueBindingShorthandScope(it) }
    }
  }

  private object VueBindingShorthandSymbolOrigin : PolySymbolOrigin {
    override val framework: FrameworkId
      get() = VueFramework.ID
  }

  private val VueBindingShorthandSymbol = ReferencingPolySymbol.create(
    VUE_BINDING_SHORTHANDS,
    "Vue Binding Shorthand",
    VueBindingShorthandSymbolOrigin,
    HTML_ATTRIBUTES
  )
}

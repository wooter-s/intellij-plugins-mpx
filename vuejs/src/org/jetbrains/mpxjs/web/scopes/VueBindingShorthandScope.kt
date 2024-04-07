// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web.scopes

import com.intellij.javascript.webSymbols.symbols.asWebSymbol
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.Processor
import com.intellij.util.asSafely
import com.intellij.webSymbols.*
import com.intellij.webSymbols.html.WebSymbolHtmlAttributeValue
import com.intellij.webSymbols.query.WebSymbolsQueryExecutorFactory
import org.jetbrains.mpxjs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.mpxjs.codeInsight.fromAsset
import org.jetbrains.mpxjs.codeInsight.template.VueTemplateScopesResolver
import org.jetbrains.mpxjs.web.VUE_BINDING_SHORTHANDS

class VueBindingShorthandScope(attribute: XmlAttribute)
  : WebSymbolsScopeWithCache<XmlAttribute, Unit>(null, attribute.project, attribute, Unit) {

  override fun provides(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == VUE_BINDING_SHORTHANDS

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)

    val tag = dataHolder.context as? XmlTag ?: return
    VueAttributeNameParser.parse(dataHolder.name, tag)
      .asSafely<VueAttributeNameParser.VueDirectiveInfo>()
      ?.takeIf { it.directiveKind == VueAttributeNameParser.VueDirectiveKind.BIND } ?: return

    val executor = WebSymbolsQueryExecutorFactory.create(dataHolder)
    val attributes = executor
      .runListSymbolsQuery(WebSymbol.HTML_ATTRIBUTES, virtualSymbols = false, expandPatterns = true,
                           scope = executor.runNameMatchQuery(WebSymbol.HTML_ELEMENTS.withName(tag.name)))
      .associateBy { it.name }

    VueTemplateScopesResolver.resolve(tag, Processor { resolveResult ->
      val jsSymbol = resolveResult.element.asSafely<JSElement>()?.asWebSymbol() as? PsiSourcedWebSymbol
      if (jsSymbol != null) {
        attributes[fromAsset(jsSymbol.name)]?.let {
          consumer(VueBindingShorthandSymbol(dataHolder, jsSymbol, it))
        }
      }
      true
    })
  }

  override fun createPointer(): Pointer<VueBindingShorthandScope> {
    val pointer = dataHolder.createSmartPointer()
    return Pointer {
      pointer.dereference()?.let { VueBindingShorthandScope(it) }
    }
  }
}

class VueBindingShorthandSymbol(private val context: XmlAttribute,
                                        jsSymbol: PsiSourcedWebSymbol,
                                        private val attrSymbol: WebSymbol)
  : PsiSourcedWebSymbolDelegate<PsiSourcedWebSymbol>(jsSymbol), CompositeWebSymbol {

  override val nameSegments: List<WebSymbolNameSegment>
    get() = listOf(WebSymbolNameSegment(0, delegate.name.length, delegate, attrSymbol))

  override val namespace: SymbolNamespace
    get() = VUE_BINDING_SHORTHANDS.namespace

  override val kind: SymbolKind
    get() = VUE_BINDING_SHORTHANDS.kind

  override val attributeValue: WebSymbolHtmlAttributeValue
    get() = WebSymbolHtmlAttributeValue.create(kind = WebSymbolHtmlAttributeValue.Kind.NO_VALUE)

  override val priority: WebSymbol.Priority
    get() = WebSymbol.Priority.HIGHEST

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget =
    attrSymbol.getDocumentationTarget(location)

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    super<PsiSourcedWebSymbolDelegate>.getNavigationTargets(project) + attrSymbol.getNavigationTargets(project)

  override fun createPointer(): Pointer<out PsiSourcedWebSymbol> {
    val contextPtr = context.createSmartPointer()
    val psiSourcedSymbolPtr = delegate.createPointer()
    val attrSymbolPtr = attrSymbol.createPointer()
    return Pointer {
      val context = contextPtr.dereference() ?: return@Pointer null
      val psiSourcedSymbol = psiSourcedSymbolPtr.dereference() ?: return@Pointer null
      val attrSymbol = attrSymbolPtr.dereference() ?: return@Pointer null
      VueBindingShorthandSymbol(context, psiSourcedSymbol, attrSymbol)
    }
  }
}

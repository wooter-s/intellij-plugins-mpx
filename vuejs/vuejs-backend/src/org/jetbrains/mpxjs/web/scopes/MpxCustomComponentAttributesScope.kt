// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web.scopes

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.js.JS_EVENTS
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.search.PsiLinkedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.xml.XmlTag
import org.jetbrains.mpxjs.lang.html.tag.ComponentJsUtils
import org.jetbrains.mpxjs.lang.html.tag.ComponentWxmlUtils
import org.jetbrains.mpxjs.lang.html.tag.CustomComponentDescriptor
import org.jetbrains.mpxjs.lang.html.tag.CustomElementDescriptorProvider
import org.jetbrains.mpxjs.model.VueEmitCall
import org.jetbrains.mpxjs.web.VueComponentSourceNavigationTarget

/**
 * Exposes properties and emitted events of the MPX custom component referenced by the current tag.
 * Event names are also exposed as complete WeChat event attributes so completion and navigation
 * work for both colon and merged syntax.
 */
class MpxCustomComponentAttributesScope private constructor(
  private val componentDeclaration: PsiElement,
) : PolySymbolScope {

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> {
    if (!params.queryExecutor.allowResolve || (kind != HTML_ATTRIBUTES && kind != JS_EVENTS)) return emptyList()

    val scriptElement = ComponentWxmlUtils.findCustomComponentDefinitionScriptElement(componentDeclaration)
                        ?: return emptyList()
    return when (kind) {
      HTML_ATTRIBUTES -> {
        val properties = ComponentJsUtils.findPropertiesItems(scriptElement)
          .orEmpty()
          .map { MpxCustomComponentAttributeSymbol(it.name.orEmpty(), listOf(it)) }
          .filter { it.name.isNotEmpty() }
        val events = ComponentJsUtils.findTriggerEventItems(scriptElement).flatMap { event ->
          eventAttributeNames(event.name).map { attributeName ->
            MpxCustomComponentAttributeSymbol(attributeName, event.sources)
          }
        }
        properties + events
      }
      JS_EVENTS -> ComponentJsUtils.findTriggerEventItems(scriptElement).map {
        MpxCustomComponentEventSymbol(it.name, it.sources)
      }
      else -> emptyList()
    }
  }

  override fun createPointer(): Pointer<out PolySymbolScope> {
    val declarationPointer = componentDeclaration.createSmartPointer()
    return Pointer {
      declarationPointer.dereference()?.let(::MpxCustomComponentAttributesScope)
    }
  }

  override fun equals(other: Any?): Boolean =
    other is MpxCustomComponentAttributesScope
    && other.componentDeclaration == componentDeclaration

  override fun hashCode(): Int = componentDeclaration.hashCode()

  companion object {
    private val EVENT_PREFIXES = listOf(
      "bind",
      "catch",
      "capture-bind",
      "capture-catch",
      "mut-bind",
    )

    private fun eventAttributeNames(eventName: String): List<String> =
      EVENT_PREFIXES.flatMap { prefix -> listOf("$prefix:$eventName", "$prefix$eventName") }

    fun create(tag: XmlTag): MpxCustomComponentAttributesScope? {
      val descriptor = CustomElementDescriptorProvider().getDescriptor(tag) as? CustomComponentDescriptor
                       ?: return null
      return MpxCustomComponentAttributesScope(descriptor.declaration)
    }
  }
}

private class MpxCustomComponentAttributeSymbol(
  override val name: String,
  private val sourceElements: List<PsiElement>,
) : PsiLinkedPolySymbol {

  override val kind: PolySymbolKind
    get() = HTML_ATTRIBUTES

  override val priority: PolySymbol.Priority
    get() = PolySymbol.Priority.HIGHEST

  override val linkedElement: PsiElement
    get() = sourceElements.first()

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    sourceElements.map(::VueComponentSourceNavigationTarget)

  override fun createPointer(): Pointer<out MpxCustomComponentAttributeSymbol> {
    val name = name
    val sourcePointers = sourceElements.map { it.createSmartPointer() }
    return Pointer {
      val sources = sourcePointers.map { it.dereference() ?: return@Pointer null }
      MpxCustomComponentAttributeSymbol(name, sources)
    }
  }

  override fun equals(other: Any?): Boolean =
    other is MpxCustomComponentAttributeSymbol
    && other.name == name
    && other.sourceElements == sourceElements

  override fun hashCode(): Int = 31 * name.hashCode() + sourceElements.hashCode()
}

private class MpxCustomComponentEventSymbol(
  override val name: String,
  private val sourceElements: List<PsiElement>,
) : VueEmitCall, PsiLinkedPolySymbol {

  override val source: PsiElement
    get() = sourceElements.first()

  override val linkedElement: PsiElement
    get() = source

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    sourceElements.map(::VueComponentSourceNavigationTarget)

  override fun createPointer(): Pointer<out MpxCustomComponentEventSymbol> {
    val name = name
    val sourcePointers = sourceElements.map { it.createSmartPointer() }
    return Pointer {
      val sources = sourcePointers.map { it.dereference() ?: return@Pointer null }
      MpxCustomComponentEventSymbol(name, sources)
    }
  }

  override fun equals(other: Any?): Boolean =
    other is MpxCustomComponentEventSymbol
    && other.name == name
    && other.sourceElements == sourceElements

  override fun hashCode(): Int = 31 * name.hashCode() + sourceElements.hashCode()
}

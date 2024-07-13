// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.codeInsight.refs

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptReferenceExpressionResolver
import com.intellij.lang.javascript.ecmascript6.types.JSTypeSignatureChooser
import com.intellij.lang.javascript.findUsages.JSReadWriteAccessDetector
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.lang.javascript.psi.resolve.WalkUpResolveProcessor
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.contextOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.Processor
import com.intellij.util.SmartList
import org.jetbrains.mpxjs.codeInsight.template.VueTemplateScopesResolver
import org.jetbrains.mpxjs.index.isScriptSetupTag
import org.jetbrains.mpxjs.lang.expr.psi.VueJSFilterReferenceExpression
import org.jetbrains.mpxjs.model.VueFilter
import org.jetbrains.mpxjs.model.VueImplicitElement
import org.jetbrains.mpxjs.model.VueModelManager
import org.jetbrains.mpxjs.model.VueModelProximityVisitor

class VueExprReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl,
                                         ignorePerformanceLimits: Boolean) :
  TypeScriptReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits) {

  companion object {
    fun resolveFiltersFromReferenceExpression(expression: VueJSFilterReferenceExpression): List<VueFilter> {
      val container = VueModelManager.findEnclosingContainer(expression)
      val filters = mutableListOf<VueFilter>()
      val referenceName = expression.referenceName
      container.acceptEntities(object : VueModelProximityVisitor() {
        override fun visitFilter(name: String, filter: VueFilter, proximity: Proximity): Boolean {
          return acceptSameProximity(proximity, name == referenceName) {
            filters.add(filter)
          }
        }
      })
      return filters
    }
  }

  override fun resolve(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
    val ref = myRef
    return when {
      expression.contextOfType<XmlTag>()?.isScriptSetupTag() == true ->
        super.resolve(expression, incompleteCode)
      myReferencedName == null -> ResolveResult.EMPTY_ARRAY
      ref is VueJSFilterReferenceExpression -> resolveFilterNameReference(ref, incompleteCode)
      myQualifier is JSThisExpression -> resolveTemplateVariable(expression)
      myQualifier == null -> {
        resolveTemplateVariable(expression)
          .ifEmpty { super.resolve(expression, incompleteCode) }
      }
      else -> super.resolve(expression, incompleteCode)
    }
  }

  override fun resolveFromIndices(localProcessor: SinkResolveProcessor<ResolveResultSink>,
                                  excludeGlobalTypeScript: Boolean,
                                  includeTypeOnlyContextSymbols: Boolean): Array<ResolveResult> =
    if (myQualifier == null) {
      val processor = WalkUpResolveProcessor(myReferencedName!!, myContainingFile, myRef)
      processor.addLocalResults(localProcessor)
      getResultsFromProcessor(processor)
    }
    else super.resolveFromIndices(localProcessor, excludeGlobalTypeScript, includeTypeOnlyContextSymbols)

  private fun resolveFilterNameReference(expression: VueJSFilterReferenceExpression, incompleteCode: Boolean): Array<ResolveResult> {
    if (!incompleteCode) {
      val results = expression.multiResolve(true)
      //expected type evaluator uses incomplete = true results so we have to cache it and reuse inside incomplete = false
      return JSTypeSignatureChooser(expression.parent as JSCallExpression).chooseOverload(results)
    }
    assert(myReferencedName != null)

    val filters = resolveFiltersFromReferenceExpression(expression)
    return filters.asSequence()
      .map {
        JSResolveResult(it.source ?: JSImplicitElementImpl.Builder(myReferencedName!!, expression)
          .forbidAstAccess().toImplicitElement())
      }
      .toList()
      .toTypedArray()
  }

  private fun resolveTemplateVariable(expression: JSReferenceExpressionImpl): Array<ResolveResult> {
    // TODO merge with Angular code
    assert(myReferencedName != null)
    val access = JSReadWriteAccessDetector.ourInstance
      .getExpressionAccess(expression)

    val results = SmartList<ResolveResult>()
    val name = myReferencedName
    VueTemplateScopesResolver.resolve(myRef, Processor { resolveResult ->
      val element = resolveResult.element as? JSPsiNamedElementBase
      if (element != null && name == element.name) {
        remapSetterGetterIfNeeded(results, resolveResult, access)
        return@Processor false
      }
      true
    })
    return results.toTypedArray()
  }

  private fun remapSetterGetterIfNeeded(results: MutableList<ResolveResult>,
                                        resolveResult: ResolveResult,
                                        access: ReadWriteAccessDetector.Access) {
    val resolvedElement = resolveResult.element
    // the goal of importUsed is to apply minimal change and preserve the other previous effects of this code;
    // namely that problemKind is always ignored;
    // there's also `resolveResult.actionScriptImport` but Vue doesn't interact with AS, so this is fine;
    // this code could probably be simplified, I'm not sure if we need so many objects
    val importUsed = if (resolveResult is JSResolveResult) resolveResult.eS6Import else null
    when (val element = if (resolvedElement is VueImplicitElement) resolvedElement.context else resolvedElement) {
      is JSFunctionItem -> {
        val add: (JSFunctionItem) -> Unit = if (resolvedElement is VueImplicitElement)
          { it -> results.add(JSResolveResult(resolvedElement.copyWithProvider(it), importUsed, null)) }
        else
          { it -> results.add(JSResolveResult(it, importUsed, null)) }

        when {
          element.isGetProperty && access == ReadWriteAccessDetector.Access.Write ->
            findPropertyAccessor(element, true, add)

          element.isSetProperty && access == ReadWriteAccessDetector.Access.Read ->
            findPropertyAccessor(element, false, add)

          else -> {
            add(element)
            if (access == ReadWriteAccessDetector.Access.ReadWrite) {
              findPropertyAccessor(element, element.isGetProperty, add)
            }
          }
        }
      }
      else -> results.add(resolveResult)
    }
  }

  private fun findPropertyAccessor(function: JSFunctionItem,
                                   isSetter: Boolean,
                                   processor: (JSFunctionItem) -> Unit) {
    val parent = function.parent as? JSClass
    val name = function.name
    if (name != null && parent != null) {
      JSClassUtils.processClassesInHierarchy(parent, true) { cls, _, _ ->
        for (`fun` in cls.functions) {
          if (name == `fun`.name && (`fun`.isGetProperty && !isSetter || `fun`.isSetProperty && isSetter)) {
            processor(`fun`)
            return@processClassesInHierarchy false
          }
        }
        true
      }
    }
  }
}

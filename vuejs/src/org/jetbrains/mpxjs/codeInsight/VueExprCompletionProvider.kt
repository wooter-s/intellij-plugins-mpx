// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.lang.ecmascript6.psi.ES6Property
import com.intellij.lang.javascript.completion.*
import com.intellij.lang.javascript.completion.JSLookupPriority.*
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.util.ProcessingContext
import com.intellij.util.Processor
import org.jetbrains.mpxjs.codeInsight.template.VueTemplateScopesResolver
import org.jetbrains.mpxjs.lang.expr.psi.VueJSFilterReferenceExpression
import org.jetbrains.mpxjs.model.VueFilter
import org.jetbrains.mpxjs.model.VueModelManager
import org.jetbrains.mpxjs.model.VueModelVisitor

//`VueExprCompletionProvider` 是一个 Kotlin 类，它继承自 `CompletionProvider<CompletionParameters>` 类。这个类主要用于提供 Vue 表达式的自动完成建议。
//
//这个类重写了 `CompletionProvider` 的 `addCompletions` 方法，这个方法在每次获取自动完成建议时都会被调用。在这个方法中，它首先获取当前位置的引用，然后根据引用的类型选择不同的处理方式：
//
//- 如果引用是 `VueJSFilterReferenceExpression`，那么它会从全局的 Vue 模型中获取所有的过滤器，并将这些过滤器作为自动完成建议添加到结果中。
//
//- 如果引用是 `JSReferenceExpressionImpl`，并且引用的限定符是 `JSThisExpression` 或者 `null`，那么它会使用 `VueTemplateScopesResolver` 来获取当前作用域中的所有元素，并将这些元素作为自动完成建议添加到结果中。
//
//此外，这个类还定义了一个 `getJSLookupPriorityOf` 方法，这个方法用于获取给定的近似度对应的查找优先级。这个优先级用于在自动完成建议中对结果进行排序。
//
//总的来说，`VueExprCompletionProvider` 类是用来提供 Vue 表达式的自动完成建议的。
class VueExprCompletionProvider : CompletionProvider<CompletionParameters>() {

  companion object {
    private val FILTERED_NON_CONTEXT_KEYWORDS = setOf("do", "class", "for", "function", "if", "import()", "switch", "throw",
                                                      "var", "let", "const", "try", "while", "with", "debugger")

    fun filterOutGenericJSResults(allowGlobalSymbols: Boolean,
                                  result: CompletionResultSet,
                                  parameters: CompletionParameters) {
      result.runRemainingContributors(parameters) { completionResult ->
        val lookupElement = completionResult.lookupElement
        // Filter out JavaScript symbols, and keywords such as 'class' and 'function'
        if (lookupElement is PrioritizedLookupElement<*>
            && lookupElement.getUserData(BaseCompletionService.LOOKUP_ELEMENT_CONTRIBUTOR) is JSCompletionContributor) {
          val priority = lookupElement.priority.toInt()
          val proximity = lookupElement.explicitProximity

          // Filter some keywords
          if ((priority == NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
               || priority == LOWEST_PRIORITY.priorityValue)
              && FILTERED_NON_CONTEXT_KEYWORDS.contains(lookupElement.lookupString))
            return@runRemainingContributors

          val maxLookupPriority = NO_RELEVANT_NO_SMARTNESS_PRIORITY

          if (!allowGlobalSymbols
              && (priority < maxLookupPriority.priorityValue
                  || (priority == maxLookupPriority.priorityValue && proximity <= maxLookupPriority.proximityValue)))
            return@runRemainingContributors
        }
        result.withRelevanceSorter(completionResult.sorter)
          .withPrefixMatcher(completionResult.prefixMatcher)
          .addElement(lookupElement)
      }
    }
  }

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    var ref = parameters.position.containingFile.findReferenceAt(parameters.offset)
    if (ref is PsiMultiReference) {
      ref = ref.references.find { r -> r is VueJSFilterReferenceExpression || r is JSReferenceExpressionImpl }
    }
    if (ref is VueJSFilterReferenceExpression) {
      val container = VueModelManager.findEnclosingContainer(ref)
      container.acceptEntities(object : VueModelVisitor() {
        override fun visitFilter(name: String, filter: VueFilter, proximity: Proximity): Boolean {
          if (proximity !== Proximity.OUT_OF_SCOPE) {
            (filter.source
             ?: JSImplicitElementImpl.Builder(name, ref).setType(JSImplicitElement.Type.Method).forbidAstAccess().toImplicitElement())
              .let { JSLookupUtilImpl.createLookupElement(it, name) }
              .let { JSCompletionUtil.withJSLookupPriority(it, getJSLookupPriorityOf(proximity)) }
              .let { result.consume(it) }
          }
          return proximity !== Proximity.OUT_OF_SCOPE
        }
      })
      result.stopHere()
    }
    else if (ref is JSReferenceExpressionImpl
             && ref.qualifier is JSThisExpression?
             && ref.parent !is ES6Property) {
      val patchedResult = result.withRelevanceSorter(JSCompletionContributor.createOwnSorter(parameters))

      if (!JSReferenceCompletionProvider.skipReferenceCompletionByContext(parameters.position)) {
        VueTemplateScopesResolver.resolve(ref, Processor { resolveResult ->
          val element = resolveResult.element as? JSPsiNamedElementBase
          if (element != null) {
            patchedResult.addElement(JSCompletionUtil.withJSLookupPriority(
              JSLookupUtilImpl.createLookupElement(element, StringUtil.notNullize(element.name)),
              if (element.name?.startsWith("$") == true)
                LOCAL_SCOPE_MAX_PRIORITY_EXOTIC
              else
                LOCAL_SCOPE_MAX_PRIORITY))
          }
          true
        })
      }

      if (ref.qualifier is JSThisExpression) {
        patchedResult.stopHere()
      }
      else {
        filterOutGenericJSResults(true, result, parameters)
      }
    }
  }

  private fun getJSLookupPriorityOf(proximity: VueModelVisitor.Proximity): JSLookupPriority =
    when (proximity) {
      VueModelVisitor.Proximity.LOCAL -> LOCAL_SCOPE_MAX_PRIORITY
      VueModelVisitor.Proximity.APP -> NESTING_LEVEL_1
      VueModelVisitor.Proximity.PLUGIN -> NESTING_LEVEL_2
      VueModelVisitor.Proximity.GLOBAL -> NESTING_LEVEL_3
      else -> LOWEST_PRIORITY
    }
}

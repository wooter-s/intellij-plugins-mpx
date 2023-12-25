// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.codeInsight.refs

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptReferenceExpressionResolver
import com.intellij.lang.javascript.ecmascript6.types.JSTypeSignatureChooser
import com.intellij.lang.javascript.findUsages.JSReadWriteAccessDetector
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSFunctionItem
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.JSThisExpression
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

  /**
   * `myQualifier`是`TypeScriptReferenceExpressionResolver`类中的一个成员变量，它表示一个JavaScript或TypeScript的引用表达式的限定符。在JavaScript和TypeScript中，一个引用表达式可能有一个限定符，这个限定符通常是一个对象或者一个模块，引用表达式是这个对象或者模块的一个属性或者方法。
   *
   * 例如，在表达式`obj.prop`中，`obj`就是限定符，`prop`就是引用表达式。在这个例子中，`myQualifier`就会是`obj`。
   *
   * 在`VueExprReferenceExpressionResolver`类中，`myQualifier`被用来判断引用表达式的上下文。例如，如果`myQualifier`是一个`JSThisExpression`，那么引用表达式就是在一个对象的方法中被引用的，如果`myQualifier`是`null`，那么引用表达式就是在全局作用域中被引用的。
   */
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

  /**
   * `resolveTemplateVariable`方法在JetBrains Vue.js插件中的作用是解析Vue模板中的变量。这个方法接收一个`JSReferenceExpressionImpl`类型的参数，然后返回一个`ResolveResult`数组，每个`ResolveResult`都表示一个可能的解析结果。
   *
   * 在这个方法的实现中，首先会调用`VueTemplateScopesResolver.resolve`方法来获取当前元素的所有作用域，并对每个作用域中的元素进行处理。如果找到了一个名字和当前元素相同的元素，那么就将这个元素添加到结果中。
   *
   * 以下是一个Vue.js代码的示例，可以展示`resolveTemplateVariable`方法的用途：
   *
   * ```vue
   * <template>
   *   <div>{{ message }}</div>
   * </template>
   *
   * <script>
   * export default {
   *   data() {
   *     return {
   *       message: 'Hello, Vue!'
   *     }
   *   }
   * }
   * </script>
   * ```
   *
   * 在这个示例中，`{{ message }}`是一个插值表达式，它引用了`message`这个变量。当你在IDE中点击或者将鼠标悬停在`message`上时，`resolveTemplateVariable`方法就会被调用，来解析`message`这个变量的定义和引用。
   */
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
    when (val element = if (resolvedElement is VueImplicitElement) resolvedElement.context else resolvedElement) {
      is JSFunctionItem -> {
        val add: (JSFunctionItem) -> Unit = if (resolvedElement is VueImplicitElement)
          { it -> results.add(JSResolveResult(resolvedElement.copyWithProvider(it))) }
        else
          { it -> results.add(JSResolveResult(it)) }
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

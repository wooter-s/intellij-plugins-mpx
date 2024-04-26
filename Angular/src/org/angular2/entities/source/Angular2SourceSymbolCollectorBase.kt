// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.util.AstLoadingFilter
import com.intellij.util.containers.Stack
import org.angular2.entities.Angular2Entity

abstract class Angular2SourceSymbolCollectorBase<T : Angular2Entity, R>(
  entityClass: Class<T>, private val source: PsiElement
) : Angular2SourceEntityListProcessor<T>(entityClass) {

  private var isFullyResolved = true
  private val dependencies = HashSet<PsiElement>()
  private val resolveQueue = Stack<PsiElement?>()

  fun collect(property: JSProperty?): Result<R> =
    if (property == null)
      createResult(true, setOf(source))
    else
      AstLoadingFilter.forceAllowTreeLoading<Result<R>, RuntimeException>(property.containingFile) {
        collect(property.value)
      }

  fun collect(variable: TypeScriptVariable?): Result<R> =
    if (variable == null)
      createResult(true, setOf(source))
    else if (variable.isConst
             && variable.isExported
             && variable.attributeList?.hasModifier(JSAttributeList.ModifierType.DECLARE) == true
             && variable.typeElement != null
    )
      AstLoadingFilter.forceAllowTreeLoading<Result<R>, RuntimeException>(variable.containingFile) {
        collect(variable.typeElement)
      }
    else
      AstLoadingFilter.forceAllowTreeLoading<Result<R>, RuntimeException>(variable.containingFile) {
        collect(variable.initializer)
      }

  private fun collect(value: PsiElement?): Result<R> {
    if (value == null) {
      return createResult(false, setOf(source))
    }
    val visited = HashSet<PsiElement>()
    processCacheDependency(source)
    resolveQueue.push(value)
    while (!resolveQueue.empty()) {
      ProgressManager.checkCanceled()
      val element = resolveQueue.pop()
      if (element == null || !visited.add(element)) {
        // Protect against cyclic references or visiting same thing several times
        continue
      }
      processCacheDependency(element)
      val children = resolve(element)
      if (children.isEmpty()) {
        element.accept(resultsVisitor)
      }
      else {
        resolveQueue.addAll(children)
      }
    }
    return createResult(isFullyResolved, dependencies)
  }

  protected abstract fun createResult(isFullyResolved: Boolean, dependencies: Set<PsiElement>): Result<R>

  final override fun processCacheDependency(element: PsiElement) {
    dependencies.add(element)
  }

  override fun processNonAcceptableEntityClass(aClass: JSClass) {
    isFullyResolved = false
  }

  override fun processAnyType() {
    isFullyResolved = false
  }

  override fun processAnyElement(node: PsiElement) {
    isFullyResolved = false
  }
}
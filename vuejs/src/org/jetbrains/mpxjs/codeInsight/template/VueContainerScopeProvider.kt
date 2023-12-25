// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.codeInsight.template

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import org.jetbrains.mpxjs.model.VueEntitiesContainer
import org.jetbrains.mpxjs.model.VueModelManager
import java.util.function.Consumer

//`VueContainerScopeProvider` 是一个 Kotlin 类，它继承自 `VueTemplateScopesProvider` 类。这个类的主要作用是为 Vue 模板提供作用域。
//
//在 Vue 文件中，你可能会在模板中使用一些变量、函数或组件。`VueContainerScopeProvider` 类就是用来提供这些元素的作用域的，它会根据元素的上下文，找到包含这个元素的容器（`VueEntitiesContainer`），并创建一个对应的作用域（`VueContainerScope`）。
//
//这个类重写了 `VueTemplateScopesProvider` 的 `getScopes` 方法，这个方法在每次获取作用域时都会被调用。在这个方法中，它首先使用 `VueModelManager.findEnclosingContainer` 方法找到包含当前元素的容器，然后创建一个 `VueContainerScope` 对象。如果没有找到容器，那么就返回一个空列表。
//
//总的来说，`VueContainerScopeProvider` 类是用来提供 Vue 模板的作用域的。
class VueContainerScopeProvider : VueTemplateScopesProvider() {

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<VueTemplateScope> {
    return VueModelManager.findEnclosingContainer(hostElement ?: element)
             ?.let { listOf(VueContainerScope(it)) }
           ?: emptyList()
  }

  private class VueContainerScope(private val myEntitiesContainer: VueEntitiesContainer)
    : VueTemplateScope(null) {

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      myEntitiesContainer.thisType
        .asRecordType()
        .properties
        .asSequence()
        .mapNotNull { it.memberSource.singleElement }
        .map { PsiElementResolveResult(it, true) }
        .forEach { consumer.accept(it) }
    }
  }
}

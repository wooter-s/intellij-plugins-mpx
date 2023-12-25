// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.codeInsight.imports

import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.ES6ImportCandidate
import com.intellij.lang.javascript.modules.imports.JSImportCandidatesBase
import com.intellij.lang.javascript.modules.imports.providers.JSCandidatesProcessor
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely
import org.jetbrains.mpxjs.codeInsight.fromAsset
import org.jetbrains.mpxjs.codeInsight.toAsset
import org.jetbrains.mpxjs.context.isVueContext
import org.jetbrains.mpxjs.model.VueModelManager
import org.jetbrains.mpxjs.model.source.VueComponents
import org.jetbrains.mpxjs.model.source.VueSourceComponent
import java.util.function.Predicate
//`VueComponentImportCandidatesProvider` 是一个 Kotlin 类，它继承自 `JSImportCandidatesBase` 类。这个类主要用于提供 Vue 组件的导入候选项。
//
//当你在 Vue 文件中引用了一个未导入的组件时，`VueComponentImportCandidatesProvider` 会被用来收集这个组件的所有可能的导入候选项。这些候选项可能包括这个组件的不同版本、不同的导入路径等。
//
//这个类重写了 `JSImportCandidatesBase` 的 `getNames` 和 `processCandidates` 方法：
//
//- `getNames` 方法用于获取所有可能的组件名称。这个方法会从全局的 Vue 模型中获取所有未注册的组件的名称，然后将这些名称转换为资产形式（即将驼峰形式转换为短横线形式），并返回这些名称。
//
//- `processCandidates` 方法用于处理给定名称的所有导入候选项。这个方法会从全局的 Vue 模型中获取给定名称的组件，然后根据这个组件的源文件创建一个 `ES6ImportCandidate`，并将这个候选项传递给给定的处理器。
//
//总的来说，`VueComponentImportCandidatesProvider` 类是用来提供 Vue 组件的导入候选项的。
// TODO 组件导入导出逻辑需要优化
class VueComponentImportCandidatesProvider(private val placeInfo: JSImportPlaceInfo) : JSImportCandidatesBase(placeInfo) {

  override fun getNames(keyFilter: Predicate<in String>): Set<String> =
    VueModelManager.getGlobal(placeInfo.place).unregistered.components
      .keys.asSequence().map { toAsset(it, true) }
      .filter { keyFilter.test(it) }.toSet()

  override fun processCandidates(name: String, processor: JSCandidatesProcessor) {
    val component = VueModelManager.getGlobal(placeInfo.place).unregistered.components[fromAsset(name)]
    if (component != null && component is VueSourceComponent) {
      val source = component.descriptor.source
      if (source is PsiFile) {
        processor.processCandidate(ES6ImportCandidate(name, source, placeInfo.place))
      }
      else if (isDefineComponentSource(source)) {
        processor.processCandidate(ES6ImportCandidate(name, source.containingFile, placeInfo.place))
      }
    }
  }

  private fun isDefineComponentSource(source: PsiElement): Boolean =
    source.context
      ?.let { if (it is JSArgumentList) it.context else it }
      ?.asSafely<JSCallExpression>()
      ?.let { VueComponents.isDefineOptionsCall(it) }
    ?: false

}

class VueComponentImportCandidatesProviderFactory : JSImportCandidatesProvider.CandidatesFactory {
  override fun createProvider(placeInfo: JSImportPlaceInfo): JSImportCandidatesProvider? =
    if (isVueContext(placeInfo.place)) VueComponentImportCandidatesProvider(placeInfo) else null

}
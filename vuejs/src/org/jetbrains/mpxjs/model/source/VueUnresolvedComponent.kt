// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.model.source

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import org.jetbrains.mpxjs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.mpxjs.model.VueComponent
import org.jetbrains.mpxjs.model.VueEntitiesContainer
import org.jetbrains.mpxjs.model.getDefaultVueComponentInstanceType

class VueUnresolvedComponent(private val context: PsiElement,
                             override val rawSource: PsiElement?,
                             override val defaultName: String?) : VueComponent {

  override val source: PsiElement? by lazy(LazyThreadSafetyMode.PUBLICATION) {
    (rawSource as? ES6ImportSpecifier)?.resolveIfImportSpecifier() ?: rawSource
  }

  override val parents: List<VueEntitiesContainer> = emptyList()

  override val thisType: JSType
    get() = getDefaultVueComponentInstanceType(context) ?: JSAnyType.get(context)

  override fun createPointer(): Pointer<VueUnresolvedComponent> {
    val context = this.context.createSmartPointer()
    val source = this.source?.createSmartPointer()
    val defaultName = this.defaultName
    return Pointer {
      val newContext = context.dereference() ?: return@Pointer null
      val newSource = source?.let { it.dereference() ?: return@Pointer null }
      VueUnresolvedComponent(newContext, newSource, defaultName)
    }
  }
}

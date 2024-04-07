// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.codeInsight.imports

import com.intellij.lang.ecmascript6.resolve.JSModuleElementsProcessor
import com.intellij.lang.ecmascript6.resolve.JSModuleExportsProvider
import com.intellij.psi.PsiElement
import org.jetbrains.mpxjs.lang.html.psi.impl.VueScriptSetupEmbeddedContentImpl

class VueModuleExportsProvider : JSModuleExportsProvider {
  override fun processExports(scope: PsiElement, processor: JSModuleElementsProcessor, weak: Boolean): Boolean {
    return true
  }

  override fun getAdditionalScopes(scope: PsiElement): Collection<PsiElement> {
    if (scope is VueScriptSetupEmbeddedContentImpl) {
      return listOfNotNull(scope.contextExportScope)
    }
    return emptyList()
  }
}
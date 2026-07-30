// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import org.jetbrains.mpxjs.codeInsight.refs.VueExprReferenceExpressionResolver
import org.jetbrains.mpxjs.lang.expr.psi.VueJSFilterReferenceExpression

internal class VuePsiDocumentationProvider : PsiDocumentationTargetProvider {

  override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
    val docSource = when (originalElement?.node?.elementType) {
      JSTokenTypes.IDENTIFIER -> originalElement?.parent
      else -> originalElement
    }
    return when (docSource) {
      is VueJSFilterReferenceExpression -> VueExprReferenceExpressionResolver
        .resolveFiltersFromReferenceExpression(docSource)
        .getOrNull(0)
        ?.getDocumentationTarget(originalElement)
      else -> null
    }
  }

}

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.inspections

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.lang.javascript.inspections.ES6UnusedImportsInspection
import com.intellij.lang.typescript.inspections.TypeScriptDefaultInspectionSuppressor
import com.intellij.psi.PsiElement

class AstroDefaultInspectionSuppressor : TypeScriptDefaultInspectionSuppressor() {
  companion object {
    val INSTANCE: InspectionSuppressor = AstroDefaultInspectionSuppressor()
  }

  override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
    return super.isSuppressedFor(element, toolId) || toolId == ES6UnusedImportsInspection.SHORT_NAME
  }
}
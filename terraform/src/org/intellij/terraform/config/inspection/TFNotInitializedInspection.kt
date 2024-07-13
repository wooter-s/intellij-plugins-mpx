// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.actions.TFInitAction
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLElementVisitor

class TFNotInitializedInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val ft = holder.file.fileType
    if (ft != TerraformFileType) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return object : HCLElementVisitor() {
      override fun visitFile(file: PsiFile) {
        super.visitFile(file)
        val initializedFix = TFInitAction.createQuickFixNotInitialized(file)
        if (initializedFix != null) {
          holder.registerProblem(file, HCLBundle.message("not.initialized.inspection.error.message"),
                                 ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                 *arrayOf(initializedFix))
        }
      }
    }
  }

}
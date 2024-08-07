// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.hints

import com.intellij.codeInsight.codeVision.CodeVisionRelativeOrdering
import com.intellij.codeInsight.hints.codeVision.ReferencesCodeVisionProvider
import com.intellij.openapi.options.advanced.AdvancedSettings.Companion.getInt
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLProperty
import java.util.concurrent.atomic.AtomicInteger

class TfReferencesCodeVisionProvider : ReferencesCodeVisionProvider() {
  override fun acceptsFile(file: PsiFile): Boolean = file.fileType is TerraformFileType

  override fun acceptsElement(element: PsiElement): Boolean = when (element) {
    is HCLBlock -> TerraformPatterns.VariableRootBlock.accepts(element)
    is HCLProperty -> TerraformPatterns.LocalProperty.accepts(element)
    else -> false
  }

  override fun getHint(element: PsiElement, file: PsiFile): String? {
    if (element !is HCLElement)
      return null
    val scope = element.getTerraformModule().getTerraformModuleScope()

    val usagesCount = AtomicInteger()
    val limit = getInt("org.intellij.terraform.code.vision.usages.limit")

    ReferencesSearch.search(ReferencesSearch.SearchParameters(element, scope, false))
      .allowParallelProcessing()
      .forEach(Processor {
        if (it == null)
          true
        else usagesCount.incrementAndGet() <= limit
      })

    val result = usagesCount.get()
    return CodeVisionInfo(
      HCLBundle.message("terraform.inlay.hints.usages.text", Math.min(result, limit), if (result > limit) 1 else 0),
      result,
      result <= limit
    ).text
  }

  override val relativeOrderings: List<CodeVisionRelativeOrdering>
    get() = emptyList()
  override val id: String
    get() = ID

  companion object {
    const val ID: String = "tf.references"
  }
}
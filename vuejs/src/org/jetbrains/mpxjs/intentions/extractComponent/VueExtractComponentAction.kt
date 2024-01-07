// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.intentions.extractComponent

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.actions.BaseRefactoringAction
import org.jetbrains.mpxjs.lang.html.VueFileType.Companion.isDotVueFile
import org.jetbrains.mpxjs.lang.html.VueLanguage

internal class VueExtractComponentAction : BaseRefactoringAction() {
  override fun isAvailableInEditorOnly(): Boolean = true

  override fun isEnabledOnElements(elements: Array<out PsiElement>): Boolean = true
  override fun isAvailableOnElementInEditorAndFile(element: PsiElement, editor: Editor, file: PsiFile, context: DataContext): Boolean {
    return VueExtractComponentIntention.getContext(editor, element) != null
  }

  override fun isAvailableForLanguage(language: Language?): Boolean = VueLanguage.INSTANCE == language

  override fun isAvailableForFile(file: PsiFile?): Boolean = file?.isDotVueFile == true

  override fun getHandler(dataContext: DataContext): RefactoringActionHandler {
    return object : RefactoringActionHandler {
      override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext?) {
        editor ?: return
        val element = PsiUtilBase.getElementAtCaret(editor) ?: return
        val context = VueExtractComponentIntention.getContext(editor, element) ?: return
        VueExtractComponentRefactoring(project, context, editor).perform()
      }

      override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
        // available only in editor
      }
    }
  }
}

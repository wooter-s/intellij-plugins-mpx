// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.codeInsight.completion

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.html.HtmlTag
import org.jetbrains.mpxjs.codeInsight.ATTR_ARGUMENT_PREFIX
import org.jetbrains.mpxjs.codeInsight.ATTR_EVENT_SHORTHAND
import org.jetbrains.mpxjs.codeInsight.ATTR_SLOT_SHORTHAND
import org.jetbrains.mpxjs.lang.html.VueLanguage

//`VueAutoPopupHandler` 是一个 Kotlin 类，它继承自 `TypedHandlerDelegate`。这个类主要用于处理在 Vue 文件中键入特定字符时的自动弹出行为。
//
//当在 Vue 文件中键入特定的字符（如属性参数前缀 `:`，事件简写 `@`，或插槽简写 `#`）时，`VueAutoPopupHandler` 会触发自动弹出控制器（`AutoPopupController`），显示相关的自动完成建议。
//
//这个类重写了 `TypedHandlerDelegate` 的 `checkAutoPopup` 方法，该方法在每次键入字符时都会被调用。在这个方法中，它首先检查当前是否已经有活动的查找（即是否已经有一个自动完成弹出窗口）。如果有，那么它就不会做任何事情。然后，它会检查当前文件是否是 Vue 文件，以及当前光标位置的元素是否在 HTML 标签内。如果这些条件都满足，那么它就会检查键入的字符是否是特定的字符（如 `:`，`@`，`#`）。如果是，那么它就会触发自动弹出控制器，显示自动完成建议。
class VueAutoPopupHandler : TypedHandlerDelegate() {
  override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
    if (LookupManager.getActiveLookup(editor) != null) return Result.CONTINUE
    if (file.language != VueLanguage.INSTANCE) return Result.CONTINUE

    val element = file.findElementAt(editor.caretModel.offset)
    if (element?.parent !is HtmlTag) return Result.CONTINUE

    // 键入特定的字符（如属性参数前缀 :，事件简写 @，或插槽简写 #）时，触发自动弹出控制器，显示相关的自动完成建议
    if (charTyped == ATTR_ARGUMENT_PREFIX
        || charTyped == ATTR_EVENT_SHORTHAND
        || charTyped == ATTR_SLOT_SHORTHAND) {
      AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
      return Result.STOP
    }

    return Result.CONTINUE
  }
}

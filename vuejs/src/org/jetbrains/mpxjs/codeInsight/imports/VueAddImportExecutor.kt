// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.codeInsight.imports

import com.intellij.lang.ecmascript6.actions.ES6AddImportExecutor
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runUndoTransparentWriteAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.psi.util.PsiEditorUtil
import org.jetbrains.mpxjs.editor.VueComponentSourceEdit
import org.jetbrains.mpxjs.model.VueModelManager
import org.jetbrains.mpxjs.model.VueRegularComponent
import org.jetbrains.mpxjs.model.source.COMPUTED_PROP
import org.jetbrains.mpxjs.model.source.METHODS_PROP

//`VueAddImportExecutor` 是一个 Kotlin 类，它继承自 `ES6AddImportExecutor` 类。这个类主要用于处理在 Vue 文件中添加导入语句的操作。
//
//当你在 Vue 文件中引用了一个未导入的元素时，`VueAddImportExecutor` 会被用来添加相应的导入语句。
//
//这个类重写了 `ES6AddImportExecutor` 的 `prepareScopeToAdd` 和 `postProcessScope` 方法：
//
//- `prepareScopeToAdd` 方法用于准备添加导入语句的范围。这个方法首先检查当前的位置是否在一个 HTML 标签的嵌入内容中，如果是，那么就返回这个位置；否则，它会查找包含当前位置的 Vue 组件，并创建这个组件的源代码编辑器（`VueComponentSourceEdit`）。然后，它会在一个写操作中获取或创建这个组件的脚本范围。
//
//- `postProcessScope` 方法用于在添加导入语句后进行后处理。这个方法首先查找包含当前位置的 Vue 组件，并创建这个组件的源代码编辑器（`VueComponentSourceEdit`）。然后，它会根据导入的元素的类型，添加相应的属性引用或函数到组件的 `methods` 或 `computed` 属性中。如果需要，它还会移动光标到新添加的函数的返回值位置。
//
//总的来说，`VueAddImportExecutor` 类是用来处理在 Vue 文件中添加导入语句的操作的。
class VueAddImportExecutor(place: PsiElement) : ES6AddImportExecutor(place) {

  override fun prepareScopeToAdd(place: PsiElement, fromExternalModule: Boolean): PsiElement? {
    if (place is JSEmbeddedContentImpl && place.context is HtmlTag) return place
    if (place !is JSReferenceExpression) return null
    ApplicationManager.getApplication().assertReadAccessAllowed()
    val component = VueModelManager.findEnclosingContainer(place) as? VueRegularComponent ?: return null
    val componentEdit = VueComponentSourceEdit.create(component) ?: return null
    val project = place.project
    return runUndoTransparentWriteAction {
      val manager = PsiDocumentManager.getInstance(project)
      val document = manager.getDocument(place.containingFile)
      if (document != null) {
        manager.commitDocument(document)
      }
      componentEdit.getOrCreateScriptScope()
    }
  }

  override fun postProcessScope(place: PsiElement, info: JSImportDescriptor, scope: PsiElement) {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    val component = VueModelManager.findEnclosingContainer(place) as? VueRegularComponent ?: return
    val componentEdit = VueComponentSourceEdit.create(component) ?: return

    if (!componentEdit.isScriptSetup()) {
      runUndoTransparentWriteAction {
        PsiDocumentManager.getInstance(place.project).commitAllDocuments()
        val element = JSStubBasedPsiTreeUtil.resolveLocally(info.effectiveName, scope) ?: return@runUndoTransparentWriteAction
        val type = JSStubBasedPsiTreeUtil.calculateMeaningfulElements(element).firstOrNull()
          ?.let { JSResolveUtil.getElementJSType(it) }
          ?.substitute()

        @Suppress("DEPRECATION")
        val editor = PsiEditorUtil.findEditor(scope)
          ?.let { InjectedLanguageUtil.getInjectedEditorForInjectedFile(it, place.containingFile) }
        val shouldPossiblyMoveCursor = editor != null && editor.caretModel.offset == place.textRange.endOffset
        if (type != null && JSTypeUtils.hasFunctionType(type, false, element)) {
          componentEdit.addClassicPropertyReference(METHODS_PROP, info.effectiveName)
        }
        else if (type == null || JSTypeUtils.isInstanceType(type)) {
          componentEdit.addClassicPropertyFunction(METHODS_PROP, info.effectiveName, "return ${info.effectiveName}")
          JSChangeUtil.createExpressionPsiWithContext("${info.effectiveName}()", place, JSCallExpression::class.java)
            ?.let { place.replace(it) }
          PsiDocumentManager.getInstance(place.project).commitAllDocuments()

          if (editor != null && shouldPossiblyMoveCursor) {
            editor.caretModel.moveCaretRelatively(2, 0, false, false, false)
          }
        }
        else {
          componentEdit.addClassicPropertyFunction(COMPUTED_PROP, info.effectiveName, "return ${info.effectiveName}")
        }
      }
    }
  }

}
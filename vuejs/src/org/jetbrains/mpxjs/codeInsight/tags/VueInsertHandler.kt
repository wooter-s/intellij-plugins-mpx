// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.codeInsight.tags

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.XmlTagInsertHandler
import com.intellij.codeInsight.editorActions.XmlTagNameSynchronizer
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.refactoring.FormatFixer
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import org.jetbrains.mpxjs.inspections.quickfixes.VueImportComponentQuickFix

//`VueInsertHandler` 是一个 Kotlin 类，它继承自 `XmlTagInsertHandler` 类。这个类主要用于处理在 Vue 文件中插入 XML 标签的操作。
//
//当你在 Vue 文件中插入一个 XML 标签时，`VueInsertHandler` 会被用来处理这个操作。它会根据插入的上下文和标签的信息，执行相应的操作。
//
//这个类重写了 `XmlTagInsertHandler` 的 `handleInsert` 方法，这个方法在每次插入标签时都会被调用。在这个方法中，它首先检查是否应该处理 XML 插入，如果是，那么就调用父类的 `handleInsert` 方法。然后，它会获取插入的元素，如果这个元素在当前文件中已经被导入，那么就不做任何事情。否则，它会查找这个元素的定义，如果这个定义在一个 Node 模块中，那么就会跳过这个模块。最后，它会提交文档的更改，并在一个写操作中添加相应的导入语句。
//
//此外，这个类还定义了一些辅助方法，如 `reformatElement`、`shouldHandleXmlInsert`、`isSkippedModule` 等，这些方法用于格式化元素、检查是否应该处理 XML 插入、检查是否应该跳过某个模块等。
//
//总的来说，`VueInsertHandler` 类是用来处理在 Vue 文件中插入 XML 标签的操作的。
class VueInsertHandler : XmlTagInsertHandler() {

  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    if (shouldHandleXmlInsert(context)) {
      super.handleInsert(context, item)
    }
    val element = WebSymbolCodeCompletionItem.getPsiElement(item)
                  ?: return
    val importedFile = element.containingFile
    if (importedFile == context.file) return
    val nodeModule = NodeModuleSearchUtil.findDependencyRoot(element.containingFile.virtualFile)
    if (isSkippedModule(nodeModule)) return

    context.commitDocument()

    XmlTagNameSynchronizer.runWithoutCancellingSyncTagsEditing(context.document) {
      val location = PsiTreeUtil.findElementOfClassAtOffset(context.file, context.startOffset, PsiElement::class.java, false)
                     ?: return@runWithoutCancellingSyncTagsEditing
      VueImportComponentQuickFix(location, item.lookupString.removePrefix("<"), element).applyFix()
      PostprocessReformattingAspect.getInstance(context.project).doPostponedFormatting()
    }
  }

  companion object {
    val INSTANCE: VueInsertHandler = VueInsertHandler()

    fun reformatElement(element: PsiElement?) {
      if (element != null && element.isValid) {
        val range = element.textRange
        FormatFixer.doReformat(element as? PsiFile ?: element.containingFile, range.startOffset, range.endOffset)
      }
    }

    private fun shouldHandleXmlInsert(context: InsertionContext): Boolean {
      val file = context.file
      if (!file.language.isKindOf(XMLLanguage.INSTANCE)) {
        return false
      }
      val element = PsiTreeUtil.findElementOfClassAtOffset(file, context.startOffset, XmlTag::class.java, false)
      return element == null || element.language.isKindOf(XMLLanguage.INSTANCE)
    }

    private fun isSkippedModule(nodeModule: VirtualFile?) =
      nodeModule != null
      && nodeModule.parent?.name == JSLibraryUtil.NODE_MODULES
      && ("vue" == nodeModule.name || "vue-router" == nodeModule.name)

  }
}

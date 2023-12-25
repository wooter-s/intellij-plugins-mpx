// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.html.highlighting

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.mpxjs.lang.LangMode
import org.jetbrains.mpxjs.lang.VueScriptLangs
import org.jetbrains.mpxjs.lang.html.VueFileElementType
import org.jetbrains.mpxjs.lang.html.VueFileType.Companion.isDotVueFile

//`VueSyntaxHighlighterFactory`类是一个Kotlin类，它继承自`SyntaxHighlighterFactory`类。这个类的主要作用是为Vue文件提供语法高亮功能。它实现了以下功能：
//
//1. **创建语法高亮器**：这个类重写了`SyntaxHighlighterFactory`的`getSyntaxHighlighter`方法，这个方法在每次获取语法高亮器时都会被调用。在这个方法中，它首先获取Vue文件的语言模式，然后调用`getSyntaxHighlighter`方法创建一个语法高亮器。
//
//2. **配置语法高亮器**：`getSyntaxHighlighter`方法接收一个项目、一个虚拟文件和一个语言模式作为参数，然后创建一个`VueFileHighlighter`对象。这个对象用于为Vue文件提供语法高亮功能。它会根据项目的JavaScript语言级别、Vue文件的语言模式、插值配置和HTML兼容模式来配置语法高亮器。
//
//总的来说，`VueSyntaxHighlighterFactory`类是用来为Vue文件提供语法高亮功能的。
class VueSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
    val langMode = VueScriptLangs.getLatestKnownLang(project, virtualFile)
    return getSyntaxHighlighter(project, virtualFile, langMode)
  }

  companion object {
    fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?, langMode: LangMode): SyntaxHighlighter =
      VueFileHighlighter(
        project?.let { JSRootConfiguration.getInstance(it).languageLevel } ?: JSLanguageLevel.getLevelForJSX(),
        langMode,
        project,
        VueFileElementType.readDelimiters(virtualFile?.name),
        virtualFile?.isDotVueFile == false
      )
  }

}

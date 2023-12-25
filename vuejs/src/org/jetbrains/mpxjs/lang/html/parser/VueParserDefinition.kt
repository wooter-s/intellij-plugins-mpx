// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.html.HTMLParser
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.html.HtmlEmbeddedContentImpl
import com.intellij.psi.tree.IFileElementType
import org.jetbrains.mpxjs.lang.LangMode
import org.jetbrains.mpxjs.lang.html.VueFileElementType
import org.jetbrains.mpxjs.lang.html.lexer.VueLexer
import org.jetbrains.mpxjs.lang.html.lexer.VueParsingLexer
import org.jetbrains.mpxjs.lang.html.psi.impl.VueFileImpl

//VueFileImpl是一个Kotlin类，它继承自HtmlFileImpl并实现了VueFile接口。这个类主要用于表示和处理Vue.js文件。  这个类中定义了以下几个方法和属性：
//getStub：这个方法返回一个VueFileStubImpl对象，表示Vue.js文件的存根。存根是源代码的轻量级表示，用于快速访问源代码的结构信息，而无需解析整个文件。
//langMode：这是一个只读属性，返回一个LangMode枚举值，表示Vue.js文件的语言模式。这个属性的值取决于文件的最后一个子元素的类型。
//getDefaultExportedName：这个方法返回一个字符串，表示Vue.js文件的默认导出名称。这个名称是根据文件的名称生成的。
//buildModuleType：这个方法接收一个PsiElement对象作为参数，返回一个JSType对象。这个方法用于构建Vue.js文件中的模块类型。
//总的来说，VueFileImpl类的主要作用是表示和处理Vue.js文件，包括获取文件的存根、语言模式、默认导出名称以及构建模块类型等。
class VueParserDefinition : HTMLParserDefinition() {

  companion object {
    fun createLexer(project: Project,
                    interpolationConfig: Pair<String, String>?,
                    htmlCompatMode: Boolean,
                    parentLangMode: LangMode? = null): Lexer {
      val level = JSRootConfiguration.getInstance(project).languageLevel
      return VueParsingLexer(
        VueLexer(if (level.isES6Compatible) level else JSLanguageLevel.ES6, project, interpolationConfig, htmlCompatMode, false),
        parentLangMode
      )
    }
  }

  override fun createLexer(project: Project): Lexer {
    return Companion.createLexer(project, null, true)
  }

  override fun getFileNodeType(): IFileElementType {
    return VueFileElementType.INSTANCE
  }

  override fun createFile(viewProvider: FileViewProvider): PsiFile {
    return VueFileImpl(viewProvider)
  }

  override fun createElement(node: ASTNode?): PsiElement {
    if (node?.elementType is VueElementTypes.EmbeddedVueContentElementType) {
      return HtmlEmbeddedContentImpl(node)
    }
    return super.createElement(node)
  }

  override fun createParser(project: Project?): HTMLParser = VueParser()
}

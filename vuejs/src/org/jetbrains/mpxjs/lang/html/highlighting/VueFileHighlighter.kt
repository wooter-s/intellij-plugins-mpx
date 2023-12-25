// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.html.highlighting

import com.intellij.ide.highlighter.HtmlFileHighlighter
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.XmlHighlighterColors.HTML_CODE
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair.pair
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.mpxjs.lang.LangMode
import org.jetbrains.mpxjs.lang.html.lexer.VueLexer
import org.jetbrains.mpxjs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_END
import org.jetbrains.mpxjs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_START

//`VueFileHighlighter` 是一个 Kotlin 类，它继承自 `HtmlFileHighlighter` 类。这个类主要用于为 Vue 文件提供语法高亮功能。
//
//这个类重写了 `HtmlFileHighlighter` 的 `getTokenHighlights` 和 `getHighlightingLexer` 方法：
//
//- `getTokenHighlights` 方法用于获取给定的 token 类型的高亮样式。这个方法首先检查 `keys` 映射中是否存在给定的 token 类型的高亮样式，如果存在，那么就返回这个样式；否则，它会调用父类的 `getTokenHighlights` 方法。
//
//- `getHighlightingLexer` 方法用于获取高亮的词法分析器。这个方法返回一个 `VueLexer` 对象，这个对象用于分析 Vue 文件的语法，并提供相应的 token。
//
//此外，这个类还定义了一个 `keys` 映射，这个映射用于存储不同的 token 类型的高亮样式。这个映射在类的初始化块中被填充。
//
//总的来说，`VueFileHighlighter` 类是用来为 Vue 文件提供语法高亮功能的。
internal class VueFileHighlighter(private val languageLevel: JSLanguageLevel,
                                  private val langMode: LangMode,
                                  private val project: Project?,
                                  private val interpolationConfig: Pair<String, String>?,
                                  private val htmlCompatMode: Boolean) : HtmlFileHighlighter() {

  override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
    keys[tokenType]?.let { return it }
    return super.getTokenHighlights(tokenType)
  }

  override fun getHighlightingLexer(): Lexer {
    return VueLexer(languageLevel, project, interpolationConfig, htmlCompatMode, true, langMode)
  }

  companion object {

    private val VUE_INTERPOLATION_DELIMITER = TextAttributesKey.createTextAttributesKey(
      "VUE.SCRIPT_DELIMITERS", DefaultLanguageHighlighterColors.SEMICOLON)

    private val keys = mutableMapOf<IElementType, Array<TextAttributesKey>>()

    private fun put(token: IElementType, vararg keysArr: TextAttributesKey) {
      @Suppress("UNCHECKED_CAST")
      keys[token] = keysArr as Array<TextAttributesKey>
    }

    init {
      // 对插值表达式的开始和结束标记进行高亮
      listOf(INTERPOLATION_START, INTERPOLATION_END).forEach { token ->
        put(token, HTML_CODE, VUE_INTERPOLATION_DELIMITER)
      }

      // 对插值表达式内的JavaScript保留字进行高亮
      JSKeywordSets.AS_RESERVED_WORDS.types.forEach { token ->
        put(token, HTML_CODE, JSHighlighter.JS_KEYWORD)
      }

      //put(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, HTML_CODE, JSHighlighter.JS_KEYWORD)
      listOf(
      //  `pair`是一个Kotlin的顶层函数，它用于创建一个`Pair`对象。`Pair`是一个可以存储两个元素的数据结构，这两个元素可以是任何类型，并且它们之间没有特定的关系。
      //
      //在你选中的代码中，`pair(JSTokenTypes.STRING_LITERAL_PART, JSHighlighter.JS_KEYWORD)`，`pair`函数接收两个参数：`JSTokenTypes.STRING_LITERAL_PART`和`JSHighlighter.JS_KEYWORD`，然后返回一个包含这两个元素的`Pair`对象。
      //
      //这个`Pair`对象后续可以通过其`first`和`second`属性来访问这两个元素。例如，如果你有一个`Pair`对象`p`，你可以通过`p.first`来访问第一个元素，通过`p.second`来访问第二个元素。
        pair(JSTokenTypes.STRING_LITERAL_PART, JSHighlighter.JS_KEYWORD)
      ).forEach { p -> put(p.first, HTML_CODE, p.second) }
    }
  }

}

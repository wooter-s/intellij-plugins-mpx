// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.html.lexer

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILeafElementType
import org.jetbrains.mpxjs.lang.LangMode
import org.jetbrains.mpxjs.lang.html.VueLanguage

/**
 * `VueLangModeMarkerElementType`是一个Kotlin类，它继承自`IElementType`并实现了`ILeafElementType`接口。这个类主要用于表示Vue.js文件中的语言模式标记。
 *
 * 在这个类中，`langMode`是一个`LangMode`枚举的实例，表示Vue.js文件的语言模式，如`DEFAULT`、`JS`、`TS`等。`IElementType`是用来表示词法分析器（Lexer）输出的token类型的。
 *
 * 这个类的主要作用是创建一个新的token类型，这个token类型表示Vue.js文件中的语言模式标记。当词法分析器在分析Vue.js文件时，如果遇到了一个语言模式标记，那么就会返回一个`VueLangModeMarkerElementType`的实例。
 *
 * 以下是一个Vue.js代码的示例，展示了`VueLangModeMarkerElementType`的用途：
 *
 * ```vue
 * <script lang="ts">
 * export default {
 *   data() {
 *     return {
 *       message: 'Hello, Vue!'
 *     }
 *   }
 * }
 * </script>
 * ```
 *
 * 在这个示例中，`<script lang="ts">`是一个语言模式标记，它表示这个`<script>`标签中的代码是TypeScript代码。当词法分析器在分析这个标记时，就会返回一个`VueLangModeMarkerElementType`的实例，这个实例的`langMode`属性是`TS`。
 */
class VueLangModeMarkerElementType(val langMode: LangMode) : IElementType("VUE_LANG_MODE_$langMode", VueLanguage.INSTANCE),
                                                             ILeafElementType {
    override fun createLeafNode(leafText: CharSequence): ASTNode {
      return LeafPsiElement(this, leafText)
    }
  }
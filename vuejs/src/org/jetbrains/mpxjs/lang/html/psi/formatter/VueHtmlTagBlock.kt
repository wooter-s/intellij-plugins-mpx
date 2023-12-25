// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.html.psi.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.formatter.xml.XmlBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.formatter.xml.XmlTagBlock
import org.jetbrains.mpxjs.lang.html.VueLanguage
import org.jetbrains.mpxjs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_START
import java.util.*

class VueHtmlTagBlock(node: ASTNode,
                      wrap: Wrap?,
                      alignment: Alignment?,
                      policy: XmlFormattingPolicy,
                      indent: Indent?,
                      preserveSpace: Boolean)
  : XmlTagBlock(node, wrap, alignment, policy, indent, preserveSpace), BlockEx {

  override fun getLanguage(): Language? {
    return HTMLLanguage.INSTANCE
  }

  override fun createTagBlock(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock {
    return VueHtmlTagBlock(child, wrap, alignment, myXmlFormattingPolicy, indent ?: Indent.getNoneIndent(),
                           isPreserveSpace)
  }

  override fun createSimpleChild(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?, range: TextRange?): XmlBlock {
    return VueHtmlBlock(child, wrap, alignment, myXmlFormattingPolicy, indent, range, isPreserveSpace)
  }

  override fun createSyntheticBlock(localResult: ArrayList<Block>, childrenIndent: Indent?): Block {
    return VueSyntheticBlock(localResult, this, Indent.getNoneIndent(), myXmlFormattingPolicy, childrenIndent, HTMLLanguage.INSTANCE)
  }

  override fun useMyFormatter(myLanguage: Language, childLanguage: Language, childPsi: PsiElement): Boolean {
    return childLanguage === VueLanguage.INSTANCE || super.useMyFormatter(myLanguage, childLanguage, childPsi)
  }
    //在编程中，Wrap 是一个常见的概念，通常用于描述如何处理超出容器边界的内容。在这段 Kotlin 代码中，`Wrap` 是 `com.intellij.formatting.Wrap` 类的一个实例，它用于控制代码格式化时的换行策略。
    //
    //`Wrap` 类有许多静态方法来创建不同类型的 `Wrap` 对象，例如 `Wrap.createWrap(WrapType type, boolean wrapFirstElement)`。`WrapType` 是一个枚举，定义了以下几种换行类型：
    //
    //- `WrapType.NONE`：不换行
    //- `WrapType.NORMAL`：如果需要，就换行
    //- `WrapType.CHOP_DOWN_IF_LONG`：如果一行太长就换行
    //- `WrapType.ALWAYS`：总是换行
    //
    //在这段代码中，`chooseWrap` 方法根据 `child` 的类型选择合适的 `Wrap` 对象。如果 `child` 的类型是 `INTERPOLATION_START`，则使用 `textWrap`，否则调用父类的 `chooseWrap` 方法。
  override fun chooseWrap(child: ASTNode?, tagBeginWrap: Wrap?, attrWrap: Wrap?, textWrap: Wrap?): Wrap? =
    if (child?.elementType == INTERPOLATION_START) textWrap
    else super.chooseWrap(child, tagBeginWrap, attrWrap, textWrap)
}

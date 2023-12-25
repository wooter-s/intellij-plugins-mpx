// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.codeInsight.refs

import com.intellij.pom.PomDeclarationSearcher
import com.intellij.pom.PomTarget
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.Consumer
import com.intellij.util.asSafely
import org.jetbrains.mpxjs.lang.html.psi.VueRefAttribute

//`VuePomDeclarationSearcher` 是一个 Kotlin 类，它继承自 `PomDeclarationSearcher` 类。这个类主要用于在 Vue 文件中查找声明。
//
//在 IntelliJ IDEA 中，`PomDeclarationSearcher` 是一个用于查找元素声明的接口。当你在编辑器中点击一个元素，或者按下快捷键（如 Ctrl+B）时，IDEA 会调用 `PomDeclarationSearcher` 来查找这个元素的声明。
//
//`VuePomDeclarationSearcher` 类重写了 `PomDeclarationSearcher` 的 `findDeclarationsAt` 方法，这个方法在每次查找声明时都会被调用。在这个方法中，它首先检查当前的元素是否是 `VueRefAttribute` 的属性值，如果是，那么它就会获取这个属性的隐式元素（即这个属性引用的元素），并将这个元素作为声明返回。
//
//总的来说，`VuePomDeclarationSearcher` 类是用来查找 Vue 文件中的元素声明的。
class VuePomDeclarationSearcher : PomDeclarationSearcher() {

  override fun findDeclarationsAt(element: PsiElement, offsetInElement: Int, consumer: Consumer<in PomTarget>) {
    if (element is XmlAttributeValue && element.parent is VueRefAttribute
        && ElementManipulators.getValueTextRange(element).contains(offsetInElement)) {
      (element.parent as VueRefAttribute).implicitElement
        ?.asSafely<VueRefAttribute.VueRefDeclaration>()
        ?.let { consumer.consume(it) }
    }
  }

}

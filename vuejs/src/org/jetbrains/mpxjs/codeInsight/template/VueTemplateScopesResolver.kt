// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.codeInsight.template

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.util.Processor
import one.util.streamex.StreamEx
import org.jetbrains.mpxjs.lang.expr.VueExprMetaLanguage
import org.jetbrains.mpxjs.lang.html.VueLanguage

object VueTemplateScopesResolver {

  // 需要把所有的变量都传入，但是发现只有部分调用到了
  // 这里resolve内部的实现，主要是调用了`VueTemplateScopesProvider`的`getScopes`方法，然后遍历这个列表，调用`processAllScopesInHierarchy`方法。
  /**
   * `resolve`函数在`VueTemplateScopesResolver`类中，它的主要功能是解析Vue模板中的变量。这个函数接收一个`PsiElement`类型的参数和一个`Processor<in ResolveResult>`类型的参数，然后返回一个`ResolveResult`数组，每个`ResolveResult`都表示一个可能的解析结果。
   *
   * 在这个函数的实现中，首先会调用`VueTemplateScopesProvider`的`getScopes`方法来获取当前元素的所有作用域，并对每个作用域中的元素进行处理。如果找到了一个名字和当前元素相同的元素，那么就将这个元素添加到结果中。
   *
   * 以下是一个Vue.js代码的示例，可以展示`resolve`方法的用途：
   *
   * ```vue
   * <template>
   *   <div>{{ message }}</div>
   * </template>
   *
   * <script>
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
   * 在这个示例中，`{{ message }}`是一个插值表达式，它引用了`message`这个变量。当你在IDE中点击或者将鼠标悬停在`message`上时，`resolve`方法就会被调用，来解析`message`这个变量的定义和引用。
   */
  fun resolve(element: PsiElement, processor: Processor<in ResolveResult>) {
    // TODO merge with Angular code
    val original = CompletionUtil.getOriginalOrSelf(element)
    if (!checkLanguage(original)) {
      return
    }
    // Woo tag  这里应该是已经被注入的了？ 为什么现在不是
    val expressionIsInjected = VueExprMetaLanguage.matches(original.containingFile.language)
    val hostElement: PsiElement?
    if (expressionIsInjected) {
      //we are working within injection
      hostElement = InjectedLanguageManager.getInstance(element.project).getInjectionHost(element)
      if (hostElement == null) {
        return
      }
    }
    else {
      hostElement = null
    }

    StreamEx.of(VueTemplateScopesProvider.EP_NAME.extensionList)
      .flatCollection { provider -> provider.getScopes(element, hostElement) }
      .findFirst { s -> !s.processAllScopesInHierarchy(processor) }
  }

  /**
   * `checkLanguage`函数的主要功能是检查给定的`PsiElement`（表示代码中的一个元素）的语言是否是`VueExprMetaLanguage`，`VueLanguage.INSTANCE`，或者是`CSSLanguage.INSTANCE`。如果是，那么函数就返回`true`。如果不是，那么函数就会检查`PsiElement`的父元素的语言，如果父元素的语言匹配，那么函数就返回`true`。如果所有的检查都不匹配，那么函数就返回`false`。
   *
   * 这个函数在`VueTemplateScopesResolver`的`resolve`方法中被使用，用于确定是否应该对给定的元素进行作用域解析。
   *
   * 以下是一个Vue.js代码的示例，可以展示`checkLanguage`函数的用途：
   *
   * ```vue
   * <template>
   *   <div>{{ message }}</div>
   * </template>
   *
   * <script>
   * export default {
   *   data() {
   *     return {
   *       message: 'Hello, Vue!'
   *     }
   *   }
   * }
   * </script>
   *
   * <style scoped>
   * h1 {
   *   color: #42b983;
   * }
   * </style>
   * ```
   *
   * 在这个示例中，`{{ message }}`是一个插值表达式，它引用了`message`这个变量。当你在IDE中点击或者将鼠标悬停在`message`上时，`checkLanguage`函数就会被调用，来检查`message`这个元素的语言是否是`VueExprMetaLanguage`，`VueLanguage.INSTANCE`，或者是`CSSLanguage.INSTANCE`。如果是，那么`resolve`方法就会对`message`这个元素进行作用域解析，来找出`message`这个变量的定义和引用。
   */
  private fun checkLanguage(element: PsiElement): Boolean {
    return element.language.let {
      VueExprMetaLanguage.matches(it) || it == VueLanguage.INSTANCE || it.isKindOf(CSSLanguage.INSTANCE)
    } || element.parent?.language.let {
      VueExprMetaLanguage.matches(it) || it == VueLanguage.INSTANCE
    }
  }

}

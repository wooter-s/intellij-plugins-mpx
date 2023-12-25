// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.codeInsight.template

import com.intellij.psi.ResolveResult
import com.intellij.util.Processor
import java.util.*
import java.util.function.Consumer

abstract class VueTemplateScope
/**
 * A scope can be created with parent scope, which contents will be included in the resolution.
 * See [VueTemplateScope.processAllScopesInHierarchy]
 */
protected constructor(val parent: VueTemplateScope?) {
  protected val children = ArrayList<VueTemplateScope>()

  init {
    @Suppress("LeakingThis")
    parent?.add(this)
  }

  private fun add(scope: VueTemplateScope) {
    this.children.add(scope)
  }
  //`Processor`是一个接口，它在JetBrains的许多项目中被广泛使用，包括IntelliJ IDEA的Vue.js插件。这个接口定义了一个`process`方法，这个方法接收一个参数并返回一个布尔值。
  //
  //`Processor`接口的主要用途是对一组元素进行处理。你可以创建一个实现了`Processor`接口的对象，然后将这个对象传递给需要处理元素的方法。这个方法会对每个元素调用`Processor`的`process`方法。
  //
  //如果`process`方法返回`true`，那么处理方法会继续处理下一个元素。如果`process`方法返回`false`，那么处理方法会停止处理并立即返回。
  //
  //以下是一个使用`Processor`的示例：
  //
  //```kotlin
  //val processor = Processor<String> { element ->
  //  println(element)
  //  true
  //}
  //
  //val elements = listOf("Hello", "Vue.js", "!")
  //elements.forEach { processor.process(it) }
  //```
  //
  //在这个示例中，我们创建了一个`Processor`对象，这个对象的`process`方法会打印每个元素并返回`true`。然后我们创建了一个包含三个字符串的列表，并对这个列表的每个元素调用`Processor`的`process`方法。这会打印出列表中的每个元素。
  /**
   * This method is called on every provided scope and allows for providing resolve results from enclosing scopes.
   */
  fun processAllScopesInHierarchy(processor: Processor<in ResolveResult>): Boolean {
    var scope: VueTemplateScope? = this
    while (scope != null) {
      if (!scope.process(processor)) {
        return false
      }
      scope = scope.parent
    }
    return true
  }

  abstract fun resolve(consumer: Consumer<in ResolveResult>)

  open fun process(processor: Processor<in ResolveResult>): Boolean {
    var found = false
    resolve(Consumer { resolveResult ->
      if (!processor.process(resolveResult)) {
        found = true
      }
    })
    return !found
  }

}

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.model.source

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.ES6QualifiedNameResolver
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.model.Pointer
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.applyIf
import com.intellij.util.asSafely
import org.jetbrains.mpxjs.codeInsight.resolveElementTo
import org.jetbrains.mpxjs.index.VueFrameworkHandler
import org.jetbrains.mpxjs.index.getVueIndexData
import org.jetbrains.mpxjs.lang.html.VueFileType.Companion.isDotVueFile
import org.jetbrains.mpxjs.libraries.componentDecorator.isComponentDecorator
import org.jetbrains.mpxjs.model.typed.VueTypedEntitiesProvider

/**
 * Basic resolve from index here (when we have the name literal and the descriptor literal/reference)
 */
class VueComponents {
  companion object {
    fun onlyLocal(elements: Collection<JSImplicitElement>): List<JSImplicitElement> {
      return elements.filter(this::isNotInLibrary)
    }

    fun meaningfulExpression(element: PsiElement?): PsiElement? {
      if (element == null) return null
      return JSStubBasedPsiTreeUtil.calculateMeaningfulElements(element)
        .firstOrNull { it !is JSEmbeddedContent }
    }

    fun isNotInLibrary(element: JSImplicitElement): Boolean {
      val file = element.containingFile.viewProvider.virtualFile
      return !JSProjectUtil.isInLibrary(file, element.project) && !JSLibraryUtil.isProbableLibraryFile(file)
    }

    fun vueMixinDescriptorFinder(implicitElement: JSImplicitElement): VueSourceEntityDescriptor? {
      getVueIndexData(implicitElement)?.descriptorQualifiedReference
        ?.takeIf { it.isNotBlank() }
        ?.let { resolveReferenceToVueComponent(implicitElement, it) }
        ?.asSafely<VueSourceEntityDescriptor>()
        ?.let { return it }

      val mixinObj = (implicitElement.parent as? JSProperty)?.parent as? JSObjectLiteralExpression
      if (mixinObj != null) return VueSourceEntityDescriptor(mixinObj)

      val call = implicitElement.parent as? JSCallExpression
      if (call != null) {
        return JSStubBasedPsiTreeUtil.findDescendants(call, JSStubElementTypes.OBJECT_LITERAL_EXPRESSION)
          .firstOrNull { (it.context as? JSArgumentList)?.context == call || (it.context == call) }
          ?.let { VueSourceEntityDescriptor(it) }
      }
      return null
    }

    fun resolveReferenceToVueComponent(element: PsiElement, reference: String): VueEntityDescriptor? {
      val context = (element as? JSImplicitElement)?.parent ?: element

      return JSStubBasedPsiTreeUtil.resolveLocally(reference, context)
               ?.let { getVueComponentFromResolve(listOf(it)) }
               ?.let { return it }
             ?: getVueComponentFromResolve(ES6QualifiedNameResolver(context, true).resolveQualifiedName(reference))
    }

    private fun getVueComponentFromResolve(result: Collection<PsiElement>): VueEntityDescriptor? {
      return result.firstNotNullOfOrNull {
        getComponentDescriptor(it.applyIf(it is JSImplicitElement) { it.context ?: return@firstNotNullOfOrNull null })
      }
    }

    fun getClassComponentDescriptor(clazz: JSClass): VueSourceEntityDescriptor =
      VueSourceEntityDescriptor(
        initializer = getComponentDecorator(clazz)?.let { getDescriptorFromDecorator(it) },
        clazz = clazz)

    fun getComponentDecorator(element: JSClass): ES6Decorator? {
      element.attributeList
        ?.decorators
        ?.find(::isComponentDecorator)
        ?.let { return it }
      return (element.context as? ES6ExportDefaultAssignment)
        ?.attributeList
        ?.decorators
        ?.find(::isComponentDecorator)
    }

    /**
     * <template>
     *   <my-component></my-component>
     * </template>
     *
     * <script>
     * import MyComponent from './MyComponent.vue'
     *
     * export default {
     *   components: {
     *     MyComponent
     *   }
     * }
     * </script>
     * 像上面这样声明组件引入的方式，会在`<script>`标签中的`components`属性中声明组件。
     * TODO mpxjs要改成从application/json里解析
     */
    fun getComponentDescriptor(element: PsiElement?): VueEntityDescriptor? =
      VueTypedEntitiesProvider.getComponentDescriptor(element)
      ?: getSourceComponentDescriptor(element)

    fun getSourceComponentDescriptor(element: PsiElement?): VueSourceEntityDescriptor? =
      when (val resolved = resolveElementTo(element, JSObjectLiteralExpression::class, JSCallExpression::class,
                                            JSClass::class, JSEmbeddedContent::class, HtmlFileImpl::class)) {
        // {...}
        is JSObjectLiteralExpression -> VueSourceEntityDescriptor(resolved)

        // Vue.extend({...})
        // defineComponent({...})
        is JSCallExpression ->
          if (isComponentDefiningCall(resolved)) {
            resolved.stubSafeCallArguments
              .getOrNull(0)
              ?.let { it as? JSObjectLiteralExpression }
              ?.let { VueSourceEntityDescriptor(it) }
          }
          else null

        // @Component({...}) class MyComponent {...}
        is JSClass ->
          VueSourceEntityDescriptor(getComponentDecorator(resolved)?.let { getDescriptorFromDecorator(it) },
                                    resolved)

        // <script setup>
        is JSEmbeddedContent ->
          VueSourceEntityDescriptor(source = resolved.containingFile)

        // Vue file without script section
        is HtmlFileImpl ->
          if (resolved.virtualFile.isDotVueFile)
            VueSourceEntityDescriptor(source = resolved)
          else null

        else -> null
      }

    @StubSafe
    fun getDescriptorFromDecorator(decorator: ES6Decorator): JSObjectLiteralExpression? {
      if (!isComponentDecorator(decorator)) return null

      if (decorator is StubBasedPsiElementBase<*>) {
        decorator.stub?.let {
          return it.findChildStubByType(JSStubElementTypes.OBJECT_LITERAL_EXPRESSION)
            ?.psi
        }
      }
      val callExpression = decorator.expression as? JSCallExpression ?: return null
      val arguments = callExpression.arguments
      if (arguments.size == 1) {
        return arguments[0] as? JSObjectLiteralExpression
      }
      return null
    }

    @StubSafe
    fun isComponentDefiningCall(callExpression: JSCallExpression): Boolean =
      //  callExpression的JSReferenceExpression 内容是createPage或者createComponent
      VueFrameworkHandler.getFunctionNameFromVueIndex(callExpression).let {
        it == DEFINE_COMPONENT_FUN || it == DEFINE_NUXT_COMPONENT_FUN || it == EXTEND_FUN || it == DEFINE_OPTIONS_FUN || it == CREATE_PAGE_FUN
      }

    @StubSafe
    fun isDefineOptionsCall(callExpression: JSCallExpression): Boolean =
      VueFrameworkHandler.getFunctionNameFromVueIndex(callExpression) == DEFINE_OPTIONS_FUN

    fun isStrictComponentDefiningCall(callExpression: JSCallExpression): Boolean =
      callExpression.methodExpression?.let {
        JSSymbolUtil.isAccurateReferenceExpressionName(it, DEFINE_COMPONENT_FUN) ||
        JSSymbolUtil.isAccurateReferenceExpressionName(it, CREATE_PAGE_FUN) ||
        JSSymbolUtil.isAccurateReferenceExpressionName(it, DEFINE_NUXT_COMPONENT_FUN) ||
        JSSymbolUtil.isAccurateReferenceExpressionName(it, VUE_NAMESPACE, EXTEND_FUN) ||
        JSSymbolUtil.isAccurateReferenceExpressionName(it, DEFINE_OPTIONS_FUN)
      } ?: false
  }
}

interface VueEntityDescriptor {
  val source: PsiElement
}

/**
 * `VueSourceEntityDescriptor`是一个Kotlin类，它在JetBrains的Vue.js插件中被用来表示一个Vue组件的描述符。这个类包含了一个Vue组件的初始化器（`initializer`）和类定义（`clazz`），以及这个组件的源元素（`source`）。
 *
 * `initializer`是一个`JSElement`，它可以是一个`JSObjectLiteralExpression`或者一个`PsiFile`。这个元素表示Vue组件的定义，它可以是一个对象字面量（用于定义组件的选项），或者是一个`.vue`文件。
 *
 * `clazz`是一个`JSClass`，它表示Vue组件的类定义。这个元素在使用`Vue.extend`或者`@Component`装饰器定义Vue组件时会被使用。
 *
 * `source`是一个`PsiElement`，它表示这个Vue组件的源元素。这个元素可以是一个`.vue`文件，或者是一个JavaScript或TypeScript文件。
 *
 * 以下是一个Vue.js代码的示例，可以展示`VueSourceEntityDescriptor`的用途：
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
 *       message: 'Hello Vue!'
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
 * 在这个示例中，`<script>`标签中的`export default`语句定义了一个Vue组件。这个组件包含一个`data`函数，这个函数返回一个对象，这个对象包含一个`message`属性。在JetBrains的Vue.js插件中，这个组件的定义会被表示为一个`VueSourceEntityDescriptor`对象。这个对象的`initializer`属性会是一个`JSObjectLiteralExpression`，表示这个组件的定义。这个对象的`source`属性会是这个`.vue`文件。
 */
class VueSourceEntityDescriptor(val initializer: JSElement? /* JSObjectLiteralExpression | PsiFile */ = null,
                                val clazz: JSClass? = null,
                                override val source: PsiElement = clazz ?: initializer!!) : VueEntityDescriptor {
  init {
    assert(initializer == null || initializer is JSObjectLiteralExpression || initializer is JSFile)
  }

  fun <T> getCachedValue(provider: (descriptor: VueSourceEntityDescriptor) -> CachedValueProvider.Result<T>): T {
    val providerKey: Key<CachedValue<T>> = CachedValuesManager.getManager(source.project).getKeyForClass(provider::class.java)
    return when {
      clazz != null -> {
        val theClass = clazz
        CachedValuesManager.getCachedValue(theClass, providerKey) {
          val descriptor = VueComponents.getClassComponentDescriptor(theClass)
          provider(descriptor)
        }
      }
      initializer != null -> {
        val theInitializer = initializer
        CachedValuesManager.getCachedValue(theInitializer, providerKey) {
          provider(VueSourceEntityDescriptor(theInitializer))
        }
      }
      else -> {
        val theSource = source
        CachedValuesManager.getCachedValue(theSource, providerKey) {
          provider(VueSourceEntityDescriptor(source = theSource))
        }
      }
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this ||
    (other is VueSourceEntityDescriptor
     && other.source == source)

  override fun hashCode(): Int =
    source.hashCode()

  fun createPointer(): Pointer<VueSourceEntityDescriptor> {
    val initializerPtr = this.initializer?.createSmartPointer()
    val clazzPtr = this.clazz?.createSmartPointer()
    val sourcePtr = this.source.createSmartPointer()
    return Pointer {
      val initializer = initializerPtr?.let { it.dereference() ?: return@Pointer null }
      val clazz = clazzPtr?.let { it.dereference() ?: return@Pointer null }
      val source = sourcePtr.dereference() ?: return@Pointer null
      VueSourceEntityDescriptor(initializer, clazz, source)
    }
  }
}

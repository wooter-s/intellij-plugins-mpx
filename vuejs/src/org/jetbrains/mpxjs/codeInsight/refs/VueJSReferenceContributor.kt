// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.codeInsight.refs

import com.intellij.lang.javascript.patterns.JSPatterns
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.CachingPolyReferenceBase
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.paths.StaticPathReferenceProvider
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.*
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiTreeUtil.getParentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import org.jetbrains.mpxjs.codeInsight.getTextIfLiteral
import org.jetbrains.mpxjs.context.isVueContext
import org.jetbrains.mpxjs.index.VUE_ID_INDEX_KEY
import org.jetbrains.mpxjs.model.VueModelManager
import org.jetbrains.mpxjs.model.source.NAME_PROP
import org.jetbrains.mpxjs.model.source.TEMPLATE_PROP
import org.jetbrains.mpxjs.model.source.VueSourceEntity

class VueJSReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(THIS_INSIDE_COMPONENT, VueComponentLocalReferenceProvider())
    registrar.registerReferenceProvider(COMPONENT_NAME, VueComponentNameReferenceProvider())
    registrar.registerReferenceProvider(TEMPLATE_ID_REF, VueTemplateIdReferenceProvider())
    registrar.registerReferenceProvider(
      JSPatterns.jsLiteral()
        .inside(
          XmlPatterns.xmlTag().withLocalName("script").withAttributeValue("name", "json")
        ),
      PathReferenceProvider()
    )
  }
}

private val THIS_INSIDE_COMPONENT: ElementPattern<out PsiElement> = createThisInsideComponentPattern()
private val COMPONENT_NAME: ElementPattern<out PsiElement> = createComponentNamePattern()
private val TEMPLATE_ID_REF = JSPatterns.jsLiteral()
  .withParent(JSPatterns.jsProperty().withName(TEMPLATE_PROP))

private fun createThisInsideComponentPattern(): ElementPattern<out PsiElement> {
  return PlatformPatterns.psiElement(JSReferenceExpression::class.java)
    .and(FilterPattern(object : ElementFilter {
      override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
        return element.asSafely<JSReferenceExpression>()
          ?.qualifier
          ?.asSafely<JSThisExpression>()
          ?.let { VueModelManager.findComponentForThisResolve(it) } != null
      }

      override fun isClassAcceptable(hintClass: Class<*>?): Boolean {
        return true
      }
    }))
}

private fun createComponentNamePattern(): ElementPattern<out PsiElement> {
  return PlatformPatterns.psiElement(JSLiteralExpression::class.java)
    .withParent(JSPatterns.jsProperty().withName(NAME_PROP))
    .and(FilterPattern(object : ElementFilter {
      override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
        if (element !is JSElement) return false
        val component = VueModelManager.findEnclosingComponent(element) as? VueSourceEntity ?: return false
        return component.initializer == element.parent?.parent
      }

      override fun isClassAcceptable(hintClass: Class<*>?): Boolean {
        return true
      }

    }))
}

private class VueTemplateIdReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    return if (getTextIfLiteral(element)?.startsWith("#") == true
               && isVueContext(element)) {
      arrayOf(VueTemplateIdReference(element as JSLiteralExpression, TextRange(2, element.textLength - 1)))
    }
    else emptyArray()
  }
}

private class VueComponentLocalReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    if (element is JSReferenceExpressionImpl) {
      return arrayOf(VueComponentLocalReference(element, ElementManipulators.getValueTextRange(element)))
    }
    return emptyArray()
  }
}

private class VueComponentLocalReference(reference: JSReferenceExpressionImpl,
                                         textRange: TextRange?)
  : CachingPolyReferenceBase<JSReferenceExpressionImpl>(reference, textRange) {

  override fun resolveInner(): Array<ResolveResult> {
    val ref = element
    val name = ref.referenceName
    if (name == null) return ResolveResult.EMPTY_ARRAY
    return ref.qualifier
             .asSafely<JSThisExpression>()
             ?.let { VueModelManager.findComponentForThisResolve(it) }
             ?.thisType
             ?.asRecordType()
             ?.findPropertySignature(name)
             ?.memberSource
             ?.allSourceElements
             ?.mapNotNull { if (it.isValid) PsiElementResolveResult(it) else null }
             ?.toTypedArray<ResolveResult>()
           ?: ResolveResult.EMPTY_ARRAY
  }
}

private class VueComponentNameReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    if (element is JSLiteralExpression) {
      return arrayOf(VueComponentNameReference(element, ElementManipulators.getValueTextRange(element)))
    }
    return emptyArray()
  }

}

private class VueComponentNameReference(element: JSLiteralExpression,
                                        rangeInElement: TextRange?) : CachingPolyReferenceBase<JSLiteralExpression>(element,
                                                                                                                    rangeInElement) {
  override fun resolveInner(): Array<ResolveResult> {
    getParentOfType(element, JSPropertyImpl::class.java, true) ?: return emptyArray()
    return arrayOf(PsiElementResolveResult(JSImplicitElementImpl(element.value.toString(), element)))
  }
}

private class VueTemplateIdReference(element: JSLiteralExpression, rangeInElement: TextRange?)
  : CachingPolyReferenceBase<JSLiteralExpression>(element, rangeInElement) {
  override fun resolveInner(): Array<ResolveResult> {
    val result = mutableListOf<ResolveResult>()
    StubIndex.getInstance().processElements(VUE_ID_INDEX_KEY, value, element.project,
                                            GlobalSearchScope.projectScope(element.project),
                                            PsiElement::class.java) { element ->
      (element as? XmlAttribute)
        ?.context
        ?.asSafely<XmlTag>()
        ?.let { result.add(PsiElementResolveResult(it)) }
      true
    }
    return result.toTypedArray()
  }
}

private class PathReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    val text = ElementManipulators.getValueText(element)
    val result = mutableListOf<PsiReference>()

    // "@src/components/HelloWorld"
    // 获取第一个域名
    //val domain = text.substring(0, text.indexOf("/"))
    if (text.startsWith("@")) {

    } else {
      // 如果是路径
      if (text.contains("/")) {
        // 处理相对路径
        StaticPathReferenceProvider(FileType.EMPTY_ARRAY).apply {
          setEndingSlashNotAllowed(false)
          setRelativePathsAllowed(true)
          createReferences(element, result, false)
        }
      }
    }

    // 检查路径是否包含文件扩展名，如果不包含，添加默认的文件扩展名.mpx
    //result.forEach { ref ->
    //  val path = (ref.element as JSLiteralExpression).stringValue
    //  path?.let { path ->
    //    val fileName = path.substringAfterLast("/")
    //    if (!fileName.contains(".")) {
    //      //val absolutePath = Path.of("$path.mpx").toAbsolutePath().toString()
    //      val virtualFile = LocalFileSystem.getInstance().findFileByPath("$path.mpx")
    //      virtualFile?.let { virtualFile ->
    //        val fileElement = PsiManager.getInstance(element.project).findDirectory(virtualFile)?.originalElement
    //        fileElement?.let { fileElement ->
    //          ref.bindToElement(fileElement)
    //        }
    //      }
    //    }
    //  }
    //}

    return result.toTypedArray()
  }
}

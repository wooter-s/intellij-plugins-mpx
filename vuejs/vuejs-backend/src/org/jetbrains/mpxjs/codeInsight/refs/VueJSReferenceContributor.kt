// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.codeInsight.refs

import com.intellij.lang.javascript.frameworks.jsx.JSXReferenceContributor
import com.intellij.lang.javascript.frameworks.jsx.JSXReferenceContributor.createPathReferenceProvider
import com.intellij.lang.javascript.patterns.JSPatterns
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.CachingPolyReferenceBase
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiTreeUtil.getParentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import com.intellij.webpack.WebpackConfigurable
import org.jetbrains.mpxjs.codeInsight.getTextIfLiteral
import org.jetbrains.mpxjs.context.isVueContext
import org.jetbrains.mpxjs.index.VUE_ID_INDEX_KEY
import org.jetbrains.mpxjs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.mpxjs.model.VueModelManager
import org.jetbrains.mpxjs.model.source.NAME_PROP
import org.jetbrains.mpxjs.model.source.TEMPLATE_PROP
import org.jetbrains.mpxjs.model.source.VueSourceEntity

class VueJSReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(THIS_INSIDE_COMPONENT, VueComponentLocalReferenceProvider())
    registrar.registerReferenceProvider(COMPONENT_NAME, VueComponentNameReferenceProvider())
    registrar.registerReferenceProvider(TEMPLATE_ID_REF, VueTemplateIdReferenceProvider())
    registrar.registerReferenceProvider(PATH_ATTRIBUTE_VALUE, createPathReferenceProvider())
    registrar.registerReferenceProvider(USING_COMPONENTS_PATH, VueUsingComponentsPathReferenceProvider())
  }
}

private val THIS_INSIDE_COMPONENT: ElementPattern<out PsiElement> = createThisInsideComponentPattern()
private val COMPONENT_NAME: ElementPattern<out PsiElement> = createComponentNamePattern()
private val TEMPLATE_ID_REF = JSPatterns.jsLiteral().withParent(JSPatterns.jsProperty().withName(TEMPLATE_PROP))
private val PATH_ATTRIBUTE_VALUE: ElementPattern<out PsiElement> = creatPathAttributeValuePattern()
private val USING_COMPONENTS_PATH: ElementPattern<out PsiElement> = createUsingComponentsPathPattern()

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

private fun creatPathAttributeValuePattern() = JSPatterns.stringTemplate()
  .withAncestor(2, PlatformPatterns.psiElement(VueJSEmbeddedExpressionContent::class.java)
    .withAncestor(2, PlatformPatterns.psiElement().withParent(JSXReferenceContributor.createPathAttributePattern(true))))
  .and(FilterPattern(object : ElementFilter {
    override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
      if (element !is JSStringTemplateExpression) return false
      val text = element.text
      return text.startsWith("/") || element.text.startsWith("`/")
    }
    override fun isClassAcceptable(hintClass: Class<*>?): Boolean = true
  }))

/**
 * 匹配 usingComponents 中的组件路径字符串
 * 例如: usingComponents: { "component-name": "./path/to/component.mpx" }
 */
private fun createUsingComponentsPathPattern(): ElementPattern<out PsiElement> {
  return PlatformPatterns.psiElement(JSLiteralExpression::class.java)
    .withParent(JSPatterns.jsProperty())  // 组件名: "路径"
    .withSuperParent(3, JSPatterns.jsProperty().withName("usingComponents"))
}

/**
 * 为 usingComponents 中的路径字符串提供文件引用
 */
private class VueUsingComponentsPathReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    val literal = element as? JSLiteralExpression ?: return PsiReference.EMPTY_ARRAY
    val value = literal.stringValue ?: return PsiReference.EMPTY_ARRAY
    if (value.isBlank()) return PsiReference.EMPTY_ARRAY

    resolveMpxPathReference(element, value)?.let { return arrayOf(it) }

    // 使用 FileReferenceSet 创建文件引用，支持相对路径解析
    val referenceSet = FileReferenceSet(
      value,
      element,
      1,  // startInElement: 跳过开头的引号
      this,
      true  // caseSensitive
    )
    @Suppress("UNCHECKED_CAST")
    return referenceSet.allReferences as Array<PsiReference>
  }
}

private fun resolveMpxPathReference(element: PsiElement, path: String): PsiReference? {
  resolveAliasMpxReference(element, path)?.let { return it }
  return resolveRelativeMpxReference(element, path)
}

private fun resolveAliasMpxReference(element: PsiElement, path: String): PsiReference? {
  if (!path.startsWith("@")) return null
  val slashIndex = path.indexOf("/")
  if (slashIndex <= 0) return null
  val alias = path.substring(0, slashIndex)
  val filePath = path.substring(slashIndex + 1)
  if (filePath.isBlank()) return null

  val config = WebpackConfigurable(element.project).configManager.resolveConfig(element)
  val aliasEntry = config.resolve.alias[alias] ?: return null
  val basePath = aliasEntry.mappings.firstOrNull() ?: return null
  val fullPath = FileUtil.toSystemIndependentName("$basePath/$filePath")
  return resolveMpxReferenceByPath(element, fullPath)
}

private fun resolveRelativeMpxReference(element: PsiElement, path: String): PsiReference? {
  if (path.startsWith("@")) return null
  val normalizedPath = FileUtil.toSystemIndependentName(path)
  return resolveMpxReferenceByPath(element, normalizedPath)
}

private fun resolveMpxReferenceByPath(element: PsiElement, path: String): PsiReference? {
  val normalized = path.trimEnd('/')
  val name = normalized.substringAfterLast('/')
  val hasExtension = name.contains('.')
  val candidates = if (hasExtension) {
    listOf(normalized)
  }
  else {
    listOf(normalized, "$normalized.mpx", "$normalized/index.mpx")
  }

  for (candidate in candidates) {
    val virtualFile = findVirtualFile(element, candidate) ?: continue
    return createSelfReference(element, virtualFile)
  }
  return null
}

private fun findVirtualFile(element: PsiElement, path: String): VirtualFile? {
  return if (FileUtil.isAbsolute(path)) {
    LocalFileSystem.getInstance().findFileByPath(path)
  }
  else {
    element.containingFile?.virtualFile?.parent?.findFileByRelativePath(path)
  }
}

private fun createSelfReference(element: PsiElement, virtualFile: VirtualFile): PsiReference? {
  val psiManager = PsiManager.getInstance(element.project)
  val target = if (virtualFile.isDirectory) {
    psiManager.findDirectory(virtualFile)
  }
  else {
    psiManager.findFile(virtualFile)
  }
  return target?.let { PsiReferenceBase.createSelfReference(element, it) }
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

private class VueComponentLocalReference(
  reference: JSReferenceExpressionImpl,
  textRange: TextRange?,
) : CachingPolyReferenceBase<JSReferenceExpressionImpl>(reference, textRange) {

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

private class VueComponentNameReference(
  element: JSLiteralExpression,
  rangeInElement: TextRange?,
) : CachingPolyReferenceBase<JSLiteralExpression>(element, rangeInElement) {
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

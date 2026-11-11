// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.codeInsight.refs

import com.intellij.lang.javascript.frameworks.jsx.JSXReferenceContributor
import com.intellij.lang.javascript.frameworks.jsx.JSXReferenceContributor.createPathReferenceProvider
import com.intellij.lang.javascript.patterns.JSPatterns
import com.intellij.lang.javascript.psi.JSAssignmentExpression
import com.intellij.lang.javascript.psi.JSDefinitionExpression
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl
import com.intellij.lang.javascript.psi.resolve.CachingPolyReferenceBase
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.ResolveResult
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
    registrar.registerReferenceProvider(COMPONENT_NAME, VueComponentNameReferenceProvider())
    registrar.registerReferenceProvider(TEMPLATE_ID_REF, VueTemplateIdReferenceProvider())
    registrar.registerReferenceProvider(PATH_ATTRIBUTE_VALUE, createPathReferenceProvider())
    registrar.registerReferenceProvider(USING_COMPONENTS_PATH, VueUsingComponentsPathReferenceProvider())
  }
}

private val COMPONENT_NAME: ElementPattern<out PsiElement> = createComponentNamePattern()
private val TEMPLATE_ID_REF = JSPatterns.jsLiteral().withParent(JSPatterns.jsProperty().withName(TEMPLATE_PROP))
private val PATH_ATTRIBUTE_VALUE: ElementPattern<out PsiElement> = creatPathAttributeValuePattern()
private val USING_COMPONENTS_PATH: ElementPattern<out PsiElement> = createUsingComponentsPathPattern()

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
 * 支持三种场景:
 * 1. 直接对象字面量: usingComponents: { "component-name": "./path/to/component.mpx" }
 * 2. 变量引用: const usingComponents = { "component-name": "./path" }; module.exports = { usingComponents }
 * 3. 动态赋值: usingComponents['toggle-env'] = '@qxsrc/components/toggle-env'
 */
private fun createUsingComponentsPathPattern(): ElementPattern<out PsiElement> {
  return PlatformPatterns.psiElement(JSLiteralExpression::class.java)
    .and(FilterPattern(object : ElementFilter {
      override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
        val literal = element as? JSLiteralExpression ?: return false
        val parent = literal.parent

        // 场景1和2: 在 JSProperty 内的路径
        if (parent is JSProperty) {
          val objectLiteral = parent.parent as? JSObjectLiteralExpression ?: return false
          val grandParent = objectLiteral.parent
          // 直接场景: usingComponents: { "comp": "path" }
          if (grandParent is JSProperty && grandParent.name == "usingComponents") return true
          // 变量场景: const usingComponents = { "comp": "path" }
          if (grandParent is JSVariable && grandParent.name == "usingComponents") return true
        }

        // 场景3: 动态赋值 usingComponents['comp'] = 'path'
        if (parent is JSAssignmentExpression) {
          val defExpr = parent.children.firstOrNull { it is JSDefinitionExpression } as? JSDefinitionExpression
          val indexedAccess = defExpr?.expression as? JSIndexedPropertyAccessExpression
          if (indexedAccess != null) {
            val qualifier = indexedAccess.qualifier as? JSReferenceExpression
            if (qualifier?.referenceName == "usingComponents") return true
          }
        }

        return false
      }

      override fun isClassAcceptable(hintClass: Class<*>?): Boolean = true
    }))
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
    // 优先尝试带扩展名的文件，避免解析到目录
    listOf("$normalized.mpx", "$normalized/index.mpx", normalized)
  }

  for (candidate in candidates) {
    val virtualFile = findVirtualFile(element, candidate) ?: continue
    // 只返回文件引用，跳过目录（除非是最后一个候选）
    if (!virtualFile.isDirectory) {
      return createSelfReference(element, virtualFile)
    }
  }
  // 如果都没找到文件，返回目录引用（用于导航）
  val virtualFile = findVirtualFile(element, normalized)
  return virtualFile?.let { createSelfReference(element, it) }
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

private class VueTemplateIdReference(
  element: JSLiteralExpression,
  rangeInElement: TextRange?,
) : CachingPolyReferenceBase<JSLiteralExpression>(element, rangeInElement) {
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

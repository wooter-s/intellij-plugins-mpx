// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.resolve

import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.isFile
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopes
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import org.intellij.prisma.ide.indexing.PRISMA_ENTITIES_INDEX_KEY
import org.intellij.prisma.ide.indexing.PRISMA_KEY_VALUE_DECL_INDEX_KEY
import org.intellij.prisma.lang.PrismaFileType
import org.intellij.prisma.lang.psi.PrismaEntityDeclaration
import org.intellij.prisma.lang.psi.PrismaKeyValueDeclaration

fun processEntityDeclarations(processor: PrismaProcessor, state: ResolveState, element: PsiElement) {
  if (!processLocalFileDeclarations(processor, state, element)) return
  if (!processGlobalEntityDeclarations(processor, state, element, getSchemaScopeWithoutCurrentFile(element))) return
}

fun processLocalFileDeclarations(
  processor: PrismaProcessor,
  state: ResolveState,
  element: PsiElement,
): Boolean {
  val file = element.containingFile
  return file.processDeclarations(processor, state, null, element)
}

fun processGlobalEntityDeclarations(
  processor: PrismaProcessor,
  state: ResolveState,
  element: PsiElement,
  scope: GlobalSearchScope = getSchemaScope(element),
): Boolean {
  if (processor is PrismaResolveProcessor) {
    return processGlobalEntityDeclarations(processor, state, element, processor.name, scope)
  }
  else {
    for (key in StubIndex.getInstance().getAllKeys(PRISMA_ENTITIES_INDEX_KEY, element.project)) {
      if (!processGlobalEntityDeclarations(processor, state, element, key, scope)) return false
    }
  }
  return true
}

private fun processGlobalEntityDeclarations(
  processor: PrismaProcessor,
  state: ResolveState,
  element: PsiElement,
  key: String,
  scope: GlobalSearchScope,
): Boolean {
  for (declaration in StubIndex.getElements(PRISMA_ENTITIES_INDEX_KEY, key, element.project, scope,
                                            PrismaEntityDeclaration::class.java)) {
    if (!processor.execute(declaration, state)) return false
  }
  return true
}

fun processKeyValueDeclarations(
  project: Project,
  processor: PrismaProcessor,
  scope: GlobalSearchScope,
): Boolean {
  for (key in StubIndex.getInstance().getAllKeys(PRISMA_KEY_VALUE_DECL_INDEX_KEY, project)) {
    for (declaration in StubIndex.getElements(PRISMA_KEY_VALUE_DECL_INDEX_KEY, key, project, scope,
                                              PrismaKeyValueDeclaration::class.java)) {
      if (!processor.execute(declaration, ResolveState.initial())) return false
    }
  }
  return true
}

fun getSchemaScope(context: PsiElement): GlobalSearchScope {
  val psiFile = context.containingFile

  return CachedValuesManager.getCachedValue(psiFile) {
    create(
      buildSchemaScope(psiFile.project, getPhysicalFile(psiFile)) ?: GlobalSearchScope.fileScope(psiFile),
      VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
    )
  }
}

fun getSchemaScopeWithoutCurrentFile(context: PsiElement): GlobalSearchScope =
  GlobalSearchScope
    .notScope(GlobalSearchScope.fileScope(context.project, getPhysicalFile(context)))
    .intersectWith(getSchemaScope(context))

private fun buildSchemaScope(project: Project, file: VirtualFile): GlobalSearchScope? {
  return findSchemaRoot(project, file)?.let { GlobalSearchScopes.directoryScope(project, it, true) }
}

private fun findSchemaRoot(project: Project, file: VirtualFile): VirtualFile? {
  var root: VirtualFile? = null
  var lastDir: VirtualFile? = null

  JSProjectUtil.processDirectoriesUpToContentRoot(project, file) {
    if (it.name == "schema" && it.parent.name == "prisma" ||
        it.children.none { file -> file.isFile && file.extension == PrismaFileType.defaultExtension }) {
      root = it
      false
    }
    else {
      lastDir = it
      true
    }
  }

  return root ?: lastDir
}

private fun getPhysicalFile(context: PsiElement) =
  context.containingFile.originalFile.viewProvider.virtualFile

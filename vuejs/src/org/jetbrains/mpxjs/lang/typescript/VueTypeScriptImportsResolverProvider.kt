// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.typescript

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig
import com.intellij.lang.typescript.tsconfig.TypeScriptFileImportsResolver
import com.intellij.lang.typescript.tsconfig.TypeScriptImportResolveContext
import com.intellij.lang.typescript.tsconfig.TypeScriptImportsResolverProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.mpxjs.context.isVueContext
import org.jetbrains.mpxjs.index.VUE_DEFAULT_EXTENSIONS_WITH_DOT
import org.jetbrains.mpxjs.index.findModule
import org.jetbrains.mpxjs.lang.html.VueFileType.Companion.isDotVueFile

class VueTypeScriptImportsResolverProvider : TypeScriptImportsResolverProvider {
  override fun isImplicitTypeScriptFile(project: Project, file: VirtualFile): Boolean {
    if (!file.isDotVueFile) return false

    val psiFile = PsiManager.getInstance(project).findFile(file) ?: return false

    return findModule(psiFile, true)?.let { DialectDetector.isTypeScript(it) } == true ||
           findModule(psiFile, false)?.let { DialectDetector.isTypeScript(it) } == true
  }

  override fun getExtensions(): Array<String> = VUE_DEFAULT_EXTENSIONS_WITH_DOT

  override fun contributeResolver(project: Project, config: TypeScriptConfig): TypeScriptFileImportsResolver {
    return VueFileImportsResolver(project, config.resolveContext, config.configFile)
  }

  override fun contributeResolver(project: Project,
                                  context: TypeScriptImportResolveContext,
                                  contextFile: VirtualFile): TypeScriptFileImportsResolver? {
    if (!isVueContext(contextFile, project)) return null

    return VueFileImportsResolver(project, context, contextFile)
  }
}
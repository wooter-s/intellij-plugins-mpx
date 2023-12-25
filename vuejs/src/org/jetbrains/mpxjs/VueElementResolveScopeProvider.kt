// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptResolveScopeProvider
import com.intellij.lang.javascript.psi.resolve.JSElementResolveScopeProvider
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.mpxjs.lang.html.VueFile
import org.jetbrains.mpxjs.lang.html.VueFileType

//在这段代码中，`VueElementResolveScopeProvider` 类只定义了 `tsProvider`，这是一个 `TypeScriptResolveScopeProvider` 的实例，用于处理 TypeScript 元素的解析范围。这是因为 Vue 文件中的 JavaScript 元素的解析范围可以由父类 `JSElementResolveScopeProvider` 提供，因此不需要额外定义一个 `jsProvider`。
class VueElementResolveScopeProvider : JSElementResolveScopeProvider {
  private val tsProvider = object : TypeScriptResolveScopeProvider() {
    override fun isApplicable(file: VirtualFile): Boolean = true

    override fun restrictByFileType(file: VirtualFile,
                                    libraryService: TypeScriptLibraryProvider,
                                    moduleAndLibraryScope: GlobalSearchScope): GlobalSearchScope {
      return super.restrictByFileType(file, libraryService, moduleAndLibraryScope).uniteWith(
        GlobalSearchScope.getScopeRestrictedByFileTypes(moduleAndLibraryScope, VueFileType.INSTANCE))
    }
  }

  override fun getElementResolveScope(element: PsiElement): GlobalSearchScope? {
    val project = element.project
    val psiFile = InjectedLanguageManager.getInstance(project).getTopLevelFile(element)
    if (psiFile !is VueFile) return null
    if (DialectDetector.isTypeScript(element)) {
      return tsProvider.getResolveScope(psiFile.viewProvider.virtualFile, project)
    }
    return null
  }
}

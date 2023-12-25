// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.mpxjs.index.VUE_FILE_EXTENSION

class VueFileType private constructor() : WebFrameworkHtmlFileType(VueLanguage.INSTANCE, "Mpx.js", "mpx") {
  companion object {
    @JvmField
    val INSTANCE: VueFileType = VueFileType()

    val PsiFile.isDotVueFile
      get() = originalFile.virtualFile?.isDotVueFile
              ?: (this is VueFile && isVueFileName(this.name))

    val VirtualFile.isDotVueFile
      get() = isVueFileName(nameSequence)

    fun isVueFileName(name: String) = isVueFileName(name as CharSequence)

    private fun isVueFileName(name: CharSequence): Boolean =
      // Woo tag 这两个方法都是判断文件名是否以 .mpx 结尾，这似乎有些重复
      name.endsWith(VUE_FILE_EXTENSION) ||
      FileTypeManager.getInstance().getAssociations(INSTANCE)
        .any { it.acceptsCharSequence(name) }

  }
}

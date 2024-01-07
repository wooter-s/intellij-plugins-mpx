// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.liveTemplate

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile
import org.jetbrains.mpxjs.VueBundle

class VueInsideTagLiveTemplateContextType : TemplateContextType(VueBundle.message("vue.live.template.context.template.tag.element")) {
  override fun isInContext(file: PsiFile, offset: Int): Boolean {
    return VueBaseLiveTemplateContextType.evaluateContext(file, offset, forAttributeInsert = true)
  }
}

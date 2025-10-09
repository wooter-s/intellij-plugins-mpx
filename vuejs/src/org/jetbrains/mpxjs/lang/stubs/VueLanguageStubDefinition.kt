// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.stubs

import com.intellij.psi.PsiFile
import com.intellij.psi.StubBuilder
import com.intellij.xml.HtmlLanguageStubVersionUtil
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.LanguageStubDefinition
import com.intellij.psi.stubs.StubElement
import org.jetbrains.mpxjs.lang.expr.parser.VueJSStubElementTypes
import org.jetbrains.mpxjs.lang.html.VueFile
import org.jetbrains.mpxjs.lang.html.parser.VueStubElementTypes
import org.jetbrains.mpxjs.lang.html.stub.impl.VueFileStubImpl

class VueLanguageStubDefinition : LanguageStubDefinition {
  override val stubVersion: Int
    get() = HtmlLanguageStubVersionUtil.getHtmlStubVersion() + VueStubElementTypes.VERSION + VueJSStubElementTypes.STUB_VERSION

  override val builder: StubBuilder
    get() = object : DefaultStubBuilder() {
      override fun createStubForFile(file: PsiFile): StubElement<*> {
        return if (file is VueFile) VueFileStubImpl(file) else super.createStubForFile(file)
      }
    }
}
// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.expr.psi

import com.intellij.polySymbols.js.PolySymbolExpressionWithExpectedTypeHolder
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.types.JSExportScope
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.tree.IElementType
import org.jetbrains.mpxjs.lang.expr.stub.VueJSEmbeddedExpressionContentStub

interface VueJSEmbeddedExpressionContent : JSEmbeddedContent, PolySymbolExpressionWithExpectedTypeHolder,
                                           JSExportScope,
                                           StubBasedPsiElement<VueJSEmbeddedExpressionContentStub> {
  override fun getIElementType(): IElementType? =
    super<StubBasedPsiElement>.iElementType
}

// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.types.JSVariableElementType
import com.intellij.psi.PsiElement
import org.jetbrains.mpxjs.lang.expr.parser.VueJSStubElementTypes.EXTERNAL_ID_PREFIX
import org.jetbrains.mpxjs.lang.expr.psi.impl.VueJSVForVariableImpl

class VueJSVForVariableElementType : JSVariableElementType("V_FOR_VARIABLE") {

  override fun construct(node: ASTNode): PsiElement = VueJSVForVariableImpl(node)
  override fun shouldCreateStub(node: ASTNode): Boolean = false

  override fun getExternalId(): String = EXTERNAL_ID_PREFIX + debugName
}
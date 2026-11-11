// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.openapi.util.TextRange
import org.jetbrains.mpxjs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.mpxjs.lang.typescript.kolar.vue.compiler.core.ExpressionNode
import org.jetbrains.mpxjs.lang.typescript.kolar.vue.compiler.core.InterpolationNode

class InterpolationNodeImpl(
  private val expressionContent: VueJSEmbeddedExpressionContent,
  private val parentRange: TextRange,
) : NodeImpl(expressionContent, parentRange),
    InterpolationNode {
  override val content: ExpressionNode by lazy {
    // TODO: support compound expressions
    SimpleExpressionNodeImpl(expressionContent, parentRange)
  }

  companion object {
    fun sourceLocationWithWhitespaces(
      node: InterpolationNode,
    ): Pair<String, Int> {
      node as InterpolationNodeImpl

      return Pair(
        node.expressionContent.parent.text,
        node.parentRange.startOffset,
      )
    }
  }
}

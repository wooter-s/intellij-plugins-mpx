// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.expr.psi

import com.intellij.lang.javascript.psi.JSCallExpression

interface VueJSFilterExpression : JSCallExpression {

  override fun getName(): String?

  val filterArgumentsList: VueJSFilterArgumentsList?

}
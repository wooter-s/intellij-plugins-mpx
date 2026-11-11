// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.typescript.kolar.vue.compiler.core

// CompilerDOM.IfNode
interface IfNode : Node {
  val branches: List<IfBranchNode>
}

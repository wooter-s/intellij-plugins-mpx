// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.model

import org.jetbrains.mpxjs.codeInsight.documentation.VueDocumentedItem

interface VueDirectiveModifier : VueDocumentedItem, VueNamedSymbol {
  val pattern: Regex? get() = null
}

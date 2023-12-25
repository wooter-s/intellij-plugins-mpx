// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect

class VueLanguage private constructor() : WebFrameworkHtmlDialect("Mpx") {
  companion object {
    val INSTANCE: VueLanguage = VueLanguage()
  }
}

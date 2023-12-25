// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.libraries.i18n

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.mpxjs.lang.VueInspectionsProvider
import org.jetbrains.mpxjs.lang.configureVueDependencies
import org.jetbrains.mpxjs.lang.getVueTestDataPath

class I18nTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/i18n"

  fun testHighlighting() {
    myFixture.configureVueDependencies("vue-i18n" to "*")
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureByFiles("highlighting.vue")
    myFixture.checkHighlighting()
  }

}
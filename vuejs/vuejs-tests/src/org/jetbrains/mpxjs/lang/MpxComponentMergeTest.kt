// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.mpxjs.lang.html.VueFileType
import org.jetbrains.mpxjs.model.source.VueComponents

class MpxComponentMergeTest : BasePlatformTestCase() {
  fun testCreateComponentIsRecognized() {
    val file = myFixture.configureByText("component.js", """
      import { createComponent } from '@mpxjs/core';
      createComponent({ properties: { title: String }, setup() { return { message: 'hello' }; } });
    """.trimIndent())
    val call = PsiTreeUtil.findChildOfType(file, JSCallExpression::class.java)!!
    assertTrue(VueComponents.isComponentDefiningCall(call))
    assertNotNull(VueComponents.getSourceComponent(call))
  }

  fun testCreatePageIsRecognized() {
    val file = myFixture.configureByText("page.js", """
      import { createPage } from '@mpxjs/core';
      createPage({ data: { title: 'page' } });
    """.trimIndent())
    val call = PsiTreeUtil.findChildOfType(file, JSCallExpression::class.java)!!
    assertTrue(VueComponents.isComponentDefiningCall(call))
    assertNotNull(VueComponents.getSourceComponent(call))
  }

  fun testMpxFileTypeRemainsSeparate() {
    val file = myFixture.configureByText("component.mpx", "<template><view wx:if=\"{{visible}}\" bindtap=\"tap\"/></template>")
    assertEquals(VueFileType.INSTANCE, file.fileType)
    assertEquals("Mpx", file.language.id)
  }
}

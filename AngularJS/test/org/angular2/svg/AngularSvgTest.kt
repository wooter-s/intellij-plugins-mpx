// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.svg

import com.intellij.util.containers.ContainerUtil
import com.intellij.webSymbols.checkListByFile
import com.intellij.webSymbols.renderLookupItems
import one.util.streamex.StreamEx
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.inspections.Angular2TemplateInspectionsProvider
import org.angular2.modules.Angular2TestModule
import org.angularjs.AngularTestUtil

class AngularSvgTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass)
  }

  fun testHighlighting() {
    Angular2TestModule.configureCopy(myFixture, Angular2TestModule.ANGULAR_COMMON_4_0_0, Angular2TestModule.ANGULAR_CORE_4_0_0)
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("svg-highlighting.component.svg", "svg-highlighting.component.ts")
    myFixture.checkHighlighting()
  }

  fun testCompletion() {
    Angular2TestModule.configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_4_0_0, Angular2TestModule.ANGULAR_CORE_4_0_0)
    myFixture.configureByFiles("svg-completion.component.svg", "svg-completion.component.ts")
    AngularTestUtil.moveToOffsetBySignature("<<caret>paths></paths>", myFixture)
    myFixture.completeBasic()
    myFixture.checkListByFile(myFixture.renderLookupItems(true, true), "svg-completion.component.txt", false)
  }

  fun testExpressionsCompletion() {
    Angular2TestModule.configureCopy(myFixture, Angular2TestModule.ANGULAR_COMMON_4_0_0, Angular2TestModule.ANGULAR_CORE_4_0_0)
    myFixture.copyDirectoryToProject(".", ".")
    myFixture.configureByFiles("svg-completion.component.svg", "svg-completion.component.ts")
    AngularTestUtil.moveToOffsetBySignature("{{<caret>item.height}}", myFixture)
    myFixture.completeBasic()
    assertEquals(StreamEx.of(
      "!\$any#any#4", "!height#number#101", "!item#null#101", "!items#null#101"
    ).sorted().toList(),
                 ContainerUtil.sorted(AngularTestUtil.renderLookupItems(myFixture, true, true, true)))
  }

  fun testExpressionsCompletion2() {
    Angular2TestModule.configureCopy(myFixture, Angular2TestModule.ANGULAR_COMMON_4_0_0, Angular2TestModule.ANGULAR_CORE_4_0_0)
    myFixture.copyDirectoryToProject(".", ".")
    myFixture.configureByFiles("svg-completion.component.svg", "svg-completion.component.ts")
    AngularTestUtil.moveToOffsetBySignature("{{item.<caret>height}}", myFixture)
    myFixture.completeBasic()
    assertEquals(StreamEx.of(
      "!foo#string#101", "!width#number#101", "constructor#Function#98", "hasOwnProperty#boolean#98", "isPrototypeOf#boolean#98",
      "propertyIsEnumerable#boolean#98", "toLocaleString#string#98", "toString#string#98", "valueOf#Object#98"
    ).sorted().toList(),
                 ContainerUtil.sorted(AngularTestUtil.renderLookupItems(myFixture, true, true, false)))
  }
}
// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.platform.backend.documentation.DocumentationData
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.mpxjs.inspections.DuplicateTagInspection
import org.jetbrains.mpxjs.inspections.MpxUnrecognizedAttributeInspection
import org.jetbrains.mpxjs.lang.html.tag.CustomComponentDescriptor

class MpxNativeTemplateHighlightingTest : BasePlatformTestCase() {

  fun testNativeTextTagQuickDocumentation() {
    myFixture.configureByText(
      "native-text-documentation.mpx",
      "<template><view><te<caret>xt>content</text></view></template>",
    )

    val documentation = DocumentationTargetProvider.EP_NAME.extensionList
      .flatMap { it.documentationTargets(myFixture.file, myFixture.caretOffset) }
      .mapNotNull { it.computeDocumentation() as? DocumentationData }

    assertTrue("Expected Quick Documentation for the wx text tag", documentation.isNotEmpty())
    assertTrue(
      "Expected Quick Documentation to come from the wx native text metadata",
      documentation.any { it.html.contains("基础库 1.0.0 开始支持") },
    )
  }

  fun testBlockAndWxsMetadataAreRecognized() {
    myFixture.addFileToProject("format.wxs", "module.exports = {}")
    enableNativeMetadataInspections()
    // The test fixture reserves the exact `</block>` token as an editor block-selection marker.
    myFixture.configureByText(
      "native-block-wxs.mpx",
      """
        <template>
          <wxs src="./format.wxs" module="format" />
          <block wx:if="{{visible}}"><view /></block >
          <block wx:else><text>{{format.value}}</text></block >
        </template>
        <script>export default { data: { visible: true } }</script>
      """.trimIndent(),
    )

    assertNoNativeMetadataProblems()
  }

  fun testSpecialWxmlElementsAndTemplateAttributesAreRecognized() {
    myFixture.addFileToProject("partial.wxml", "<view />")
    myFixture.addFileToProject("format.qs", "module.exports = {}")
    enableNativeMetadataInspections()
    myFixture.configureByText(
      "native-special-elements.mpx",
      """
        <template>
          <import src="./partial.wxml" />
          <include src="./partial.wxml" />
          <qs src="./format.qs" module="format" />
          <page-meta background-text-style="dark" scroll-duration="300">
            <navigation-bar title="Title" loading front-color="#ffffff" />
          </page-meta>
          <template name="card"><view /></template>
          <template is="card" data="{{item}}" />
        </template>
        <script>export default { data: { item: {} } }</script>
      """.trimIndent(),
    )

    assertNoNativeMetadataProblems()
  }

  fun testNewNativeElementAttributesAreRecognized() {
    enableNativeMetadataInspections()
    myFixture.configureByText(
      "new-native-elements.mpx",
      """
        <template>
          <pan-gesture-handler
            tag="drag"
            worklet:ongesture="handleGesture"
            worklet:should-response-on-move="shouldRespond"
            simultaneous-handlers="{{handlers}}"
          />
          <selection disable-context-menu />
          <official-account-publish />
        </template>
      """.trimIndent(),
    )

    assertNoNativeMetadataProblems()
  }

  fun testUnrecognizedAttributeInspectionUsesMpxDescriptor() {
    myFixture.enableInspections(MpxUnrecognizedAttributeInspection::class.java)
    myFixture.configureByText(
      "invalid-native-attribute.mpx",
      "<template><text not-a-text-property /></template>",
    )

    val problems = myFixture.doHighlighting().filter {
      it.description.orEmpty().contains("not-a-text-property")
    }
    assertEquals("Expected one wx native attribute warning", 1, problems.size)
  }

  fun testNativeElementsDoNotRequireContextDependentAttributesGlobally() {
    myFixture.enableInspections(RequiredAttributesInspection::class.java)
    myFixture.configureByText(
      "optional-native-attributes.mpx",
      """
        <template>
          <text>车辆编号：</text>
          <input />
          <map />
        </template>
      """.trimIndent(),
    )

    val missingAttributes = myFixture.doHighlighting().filter {
      it.description.orEmpty().contains("doesn't have required attribute")
    }
    assertTrue(
      "Native component attributes must not be globally required: ${missingAttributes.map { it.description }}",
      missingAttributes.isEmpty(),
    )
  }

  fun testUnrecognizedAttributeInspectionIgnoresStandardHtmlAndRegisteredCustomComponents() {
    myFixture.addFileToProject(
      "components/vehicle-card/index.mpx",
      """
        <template><view /></template>
        <script>createComponent({})</script>
      """.trimIndent(),
    )
    myFixture.enableInspections(MpxUnrecognizedAttributeInspection::class.java)
    myFixture.configureByText(
      "inspection-boundaries.mpx",
      """
        <template>
          <div arbitrary-html-attribute="value" />
          <vehicle-reservation-card arbitrary-custom-attribute="value" />
        </template>
        <script name="json">
        module.exports = {
          usingComponents: {
            "vehicle-reservation-card": "./components/vehicle-card/index.mpx"
          }
        }
        </script>
      """.trimIndent(),
    )

    val customTag = PsiTreeUtil.findChildrenOfType(myFixture.file, XmlTag::class.java)
      .firstOrNull { it.name == "vehicle-reservation-card" }
      ?: error("Expected the registered custom component tag")
    assertTrue(
      "Expected the registered tag to use CustomComponentDescriptor, got ${customTag.descriptor?.javaClass?.name}",
      customTag.descriptor is CustomComponentDescriptor,
    )

    val boundaryProblems = myFixture.doHighlighting().filter {
      val description = it.description.orEmpty()
      "arbitrary-html-attribute" in description || "arbitrary-custom-attribute" in description
    }
    assertTrue(
      "Standard HTML and registered custom components must not be inspected as native MPX elements: ${
        boundaryProblems.map { it.description }
      }",
      boundaryProblems.isEmpty(),
    )
  }

  fun testNativeMapMustacheAttributesDoNotProduceSyntaxErrors() {
    myFixture.configureByText(
      "home-map.mpx",
      """
        <template>
          <map
            longitude="{{centerLng}}"
            latitude="{{centerLat}}"
            scale="{{scale}}"
            markers="{{markerList}}"
            polygons="{{polygonList}}"
            polyline="{{routePolyline}}"
            show-location
            style="{{dynamicStyle}}"
            bind:markertap="handleMarkerTap"
          />
        </template>
        <script>
        export default {
          data: {
            centerLng: 0,
            centerLat: 0,
            scale: 16,
            markerList: [],
            polygonList: [],
            routePolyline: [],
            dynamicStyle: ''
          },
          methods: {
            handleMarkerTap() {}
          }
        }
        </script>
      """.trimIndent(),
    )

    val errors = myFixture.doHighlighting().filter { it.severity == HighlightSeverity.ERROR }
    assertTrue(
      "Expected wx native attributes to parse without errors, but got: ${
        errors.map {
          "${it.description} at ${it.startOffset}..${it.endOffset}: " +
          myFixture.file.text.substring(it.startOffset, it.endOffset)
        }
      }",
      errors.isEmpty(),
    )
  }

  fun testJsonConfigScriptDoesNotDuplicateTheComponentScript() {
    myFixture.enableInspections(DuplicateTagInspection::class.java)
    myFixture.configureByText(
      "component.mpx",
      """
        <template><view /></template>
        <script>export default {}</script>
        <script name="json">{ "component": true }</script>
      """.trimIndent(),
    )

    val duplicates = myFixture.doHighlighting().filter {
      it.description?.contains("Duplicated top-level tag") == true
    }
    assertTrue(
      "The MPX JSON config script must have a separate top-level role: ${duplicates.map { it.description }}",
      duplicates.isEmpty(),
    )
  }

  fun testMapCalloutAttributesAreRecognized() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java)
    myFixture.configureByText(
      "map-callout.mpx",
      """
        <template>
          <map>
            <cover-view slot="callout">
              <cover-view wx:if="{{selectedMarkerId}}" marker-id="{{selectedMarkerId}}" />
            </cover-view>
          </map>
        </template>
      """.trimIndent(),
    )

    val parsingProblems = myFixture.doHighlighting().filter {
      val description = it.description.orEmpty()
      description.contains("is not allowed here") || description.contains("is not a valid value")
    }
    assertTrue(
      "The map callout attributes must be recognized: ${parsingProblems.map { it.description }}",
      parsingProblems.isEmpty(),
    )
  }

  fun testPostCssStyleAndCommenterLoadWithoutPrivateModuleLinkage() {
    myFixture.configureByText(
      "postcss-style.mpx",
      """
        <template><view class="root" /></template>
        <style lang="postcss">
        .ro<caret>ot {
          color: red;
        }
        </style>
      """.trimIndent(),
    )

    val errors = myFixture.doHighlighting().filter { it.severity == HighlightSeverity.ERROR }
    assertTrue(
      "Expected embedded PostCSS to load without errors, but got: ${errors.map { it.description }}",
      errors.isEmpty(),
    )

    val commenterProvider = MultipleLangCommentProvider.EP_NAME.extensionList
      .firstOrNull { it.javaClass.name == "org.jetbrains.mpxjs.editor.VueCommenterProvider" }
    assertNotNull("Expected the MPX multi-language commenter to load", commenterProvider)
    assertTrue(
      "Expected the MPX commenter to accept .mpx files",
      commenterProvider!!.canProcess(myFixture.file, myFixture.file.viewProvider),
    )
  }

  fun testDuplicateComponentScriptsAreStillReported() {
    myFixture.enableInspections(DuplicateTagInspection::class.java)
    myFixture.configureByText(
      "component.mpx",
      """
        <template><view /></template>
        <script>export default {}</script>
        <script>export default {}</script>
      """.trimIndent(),
    )

    val duplicates = myFixture.doHighlighting().filter {
      it.description?.contains("Duplicated top-level tag") == true
    }
    assertFalse("Two component scripts must still be reported as duplicates", duplicates.isEmpty())
  }

  private fun enableNativeMetadataInspections() {
    myFixture.enableInspections(
      HtmlUnknownTagInspection::class.java,
      HtmlUnknownAttributeInspection::class.java,
      MpxUnrecognizedAttributeInspection::class.java,
    )
  }

  private fun assertNoNativeMetadataProblems() {
    val problems = myFixture.doHighlighting().filter {
      val description = it.description.orEmpty().lowercase()
      description.contains("unknown html tag") ||
      description.contains("is not allowed here") ||
      description.contains("is not a valid value") ||
      description.contains("unrecognized attribute")
    }
    assertTrue(
      "Expected MPX native tags and attributes to be recognized: ${problems.map { it.description }}",
      problems.isEmpty(),
    )
  }
}

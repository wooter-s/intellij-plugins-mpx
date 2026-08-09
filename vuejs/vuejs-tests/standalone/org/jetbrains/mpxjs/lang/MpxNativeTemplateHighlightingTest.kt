// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.mpxjs.inspections.DuplicateTagInspection

class MpxNativeTemplateHighlightingTest : BasePlatformTestCase() {

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
      "Expected MPX native attributes to parse without errors, but got: ${
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
}

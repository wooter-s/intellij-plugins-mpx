// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang

import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.html.HTML_ELEMENTS
import com.intellij.polySymbols.html.elements.HtmlElementSymbolDescriptor
import com.intellij.polySymbols.js.JS_EVENTS
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.mpxjs.VuejsIcons
import org.jetbrains.mpxjs.web.WX_NATIVE_LIBRARY
import org.jetbrains.mpxjs.web.isWxNativeSymbol

/**
 * Verifies the actual MPX Web-Types symbols used by native mini-program elements.
 * Merely checking for the absence of an unknown-tag warning is insufficient because
 * several native element names can otherwise fall back to standard HTML/SVG symbols.
 */
class MpxNativeElementWebTypesTest : BasePlatformTestCase() {

  override fun setUp() {
    super.setUp()
    myFixture.addFileToProject(
      "package.json",
      """
        {
          "dependencies": {
            "@mpxjs/core": "^2.9.0",
            "vue": "^2.7.0"
          }
        }
      """.trimIndent(),
    )
    myFixture.addFileToProject(
      "node_modules/vue/package.json",
      """{ "name": "vue", "version": "2.7.14" }""",
    )
  }

  fun testTextUsesWechatDescriptorAndCompletesNativeAttributes() {
    myFixture.configureByText(
      "native-text.mpx",
      """
        <template>
          <text>content</text>
        </template>
      """.trimIndent(),
    )

    val descriptor = assertWechatDescriptor(findTag("text"))
    assertContainsElements(
      descriptor.runListSymbolsQuery(HTML_ATTRIBUTES, true).map { it.name },
      "selectable",
      "user-select",
      "space",
      "decode",
    )

    configureTemplate("<text user-<caret> />")
    myFixture.completeBasic()
    myFixture.checkResult(
      """
        <template>
          <text user-select />
        </template>
      """.trimIndent(),
    )
  }

  fun testTextIsOfferedInTagCompletion() {
    configureTemplate("<te<caret>")
    myFixture.completeBasic()

    assertContainsElements(myFixture.lookupElementStrings.orEmpty(), "text")
    val presentation = lookupPresentation("text")
    assertEquals("Wrong completion source for <text>", WX_NATIVE_LIBRARY, presentation.typeText)
    assertSame("Wrong completion icon for <text>", VuejsIcons.Wx, presentation.icon)
  }

  fun testWxNativeElementsAreNotOfferedOutsideMpxContext() {
    myFixture.configureByText("plain.html", "<te<caret>")
    myFixture.completeBasic()

    assertDoesntContain(myFixture.lookupElementStrings.orEmpty(), "text")

    myFixture.configureByText("plain.html", "<text />")
    val descriptor = findTag("text").descriptor as? HtmlElementSymbolDescriptor
    assertFalse("Plain HTML must not resolve <text> from wx Web-Types", descriptor?.symbol?.isWxNativeSymbol() == true)
  }

  fun testRegisteredCustomComponentDoesNotUseWxPresentation() {
    myFixture.addFileToProject(
      "components/vehicle-card/index.mpx",
      """
        <template><view /></template>
        <script>createComponent({})</script>
      """.trimIndent(),
    )
    myFixture.configureByText(
      "custom-component-completion.mpx",
      """
        <template>
          <vehicle-<caret>
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
    myFixture.completeBasic()

    val presentation = lookupPresentation("vehicle-reservation-card")
    assertFalse("Custom component completion must not be labelled as wx", presentation.typeText == WX_NATIVE_LIBRARY)
    assertNotSame("Custom component completion must not use the wx icon", VuejsIcons.Wx, presentation.icon)
  }

  fun testMapCompletesNativePropertyAndExposesEvent() {
    configureTemplate("<map show-<caret> />")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings.orEmpty(), "show-location")

    configureTemplate("<map />")
    assertContainsElements(
      assertWechatDescriptor(findTag("map"))
        .runListSymbolsQuery(JS_EVENTS, true)
        .map { it.name },
      "regionchange",
    )
  }

  fun testInputExposesNativeEvent() {
    configureTemplate("<input />")
    assertContainsElements(
      assertWechatDescriptor(findTag("input"))
        .runListSymbolsQuery(JS_EVENTS, true)
        .map { it.name },
      "keyboardheightchange",
    )
  }

  fun testHtmlNameCollisionsPreferMpxNativeDescriptors() {
    val nativeTags = listOf(
      "audio",
      "button",
      "canvas",
      "form",
      "input",
      "label",
      "map",
      "progress",
      "span",
      "textarea",
      "video",
    )
    myFixture.configureByText(
      "native-html-name-collisions.mpx",
      buildString {
        appendLine("<template>")
        nativeTags.forEach { appendLine("  <$it />") }
        appendLine("</template>")
      },
    )

    val tagsByName = PsiTreeUtil.findChildrenOfType(myFixture.file, XmlTag::class.java)
      .associateBy { it.name }
    nativeTags.forEach { tagName ->
      val tag = tagsByName[tagName]
      assertNotNull("Cannot find <$tagName> in ${myFixture.file.name}", tag)
      assertWechatDescriptor(tag!!)
    }
  }

  fun testAllWechatNativeElementsUseWxDescriptors() {
    myFixture.configureByText(
      "all-wechat-native-elements.mpx",
      buildString {
        appendLine("<template>")
        OFFICIAL_WECHAT_NATIVE_TAGS.forEach { appendLine("  <$it />") }
        appendLine("</template>")
      },
    )

    val tagsByName = PsiTreeUtil.findChildrenOfType(myFixture.file, XmlTag::class.java)
      .associateBy { it.name }
    OFFICIAL_WECHAT_NATIVE_TAGS.forEach { tagName ->
      val tag = tagsByName[tagName]
      assertNotNull("Cannot find <$tagName> in ${myFixture.file.name}", tag)
      assertWechatDescriptor(tag!!)
    }
  }

  fun testWorkletGestureHandlerCompletesDirectEventAttribute() {
    configureTemplate("""<pan-gesture-handler worklet:onge<caret>="handlePan" />""")
    myFixture.completeBasic()
    myFixture.checkResult(
      """
        <template>
          <pan-gesture-handler worklet:ongesture="handlePan" />
        </template>
      """.trimIndent(),
    )
  }

  private fun configureTemplate(content: String) {
    myFixture.configureByText(
      "native-completion.mpx",
      """
        <template>
          $content
        </template>
      """.trimIndent(),
    )
  }

  private fun findTag(name: String): XmlTag =
    PsiTreeUtil.findChildrenOfType(myFixture.file, XmlTag::class.java)
      .firstOrNull { it.name == name }
    ?: error("Cannot find <$name> in ${myFixture.file.name}")

  private fun lookupPresentation(name: String): LookupElementPresentation {
    val lookup = myFixture.lookupElements
      ?.firstOrNull { it.lookupString == name }
      ?: error("Cannot find `$name` in completion: ${myFixture.lookupElementStrings.orEmpty()}")
    return LookupElementPresentation().also { lookup.renderElement(it) }
  }

  private fun assertWechatDescriptor(tag: XmlTag): HtmlElementSymbolDescriptor {
    val descriptor = tag.descriptor
    assertTrue(
      "Expected <${tag.name}> to use HtmlElementSymbolDescriptor, got ${descriptor?.javaClass?.name}",
      descriptor is HtmlElementSymbolDescriptor,
    )
    descriptor as HtmlElementSymbolDescriptor

    val symbol = descriptor.symbol
    assertNotNull("Expected <${tag.name}> descriptor to expose a PolySymbol", symbol)
    assertEquals("Wrong symbol kind for <${tag.name}>", HTML_ELEMENTS, symbol.kind)
    assertTrue("Standard HTML/SVG descriptor won for <${tag.name}>", symbol.isWxNativeSymbol())

    val documentation = symbol.getDocumentationTarget(tag) as? PolySymbolDocumentationTarget
    assertNotNull("Expected Web-Types documentation for <${tag.name}>", documentation)
    assertEquals(
      "Wrong documentation library for <${tag.name}>",
      WX_NATIVE_LIBRARY,
      documentation!!.documentation.library,
    )
    assertEquals(
      "Wrong documentation source for <${tag.name}>",
      "https://developers.weixin.qq.com/miniprogram/dev/component/${tag.name}.html",
      documentation.documentation.docUrl,
    )
    return descriptor
  }

  companion object {
    /**
     * Current ordinary component pages extracted from the official WeChat
     * component index. WXML-only extensions such as block/import/include are
     * covered separately because their documentation URLs use other sections.
     */
    private val OFFICIAL_WECHAT_NATIVE_TAGS = listOf(
      "view",
      "scroll-view",
      "swiper",
      "swiper-item",
      "match-media",
      "movable-area",
      "movable-view",
      "cover-view",
      "cover-image",
      "page-container",
      "share-element",
      "root-portal",
      "grid-view",
      "list-view",
      "sticky-header",
      "sticky-section",
      "nested-scroll-body",
      "nested-scroll-header",
      "draggable-sheet",
      "grid-builder",
      "list-builder",
      "snapshot",
      "span",
      "open-container",
      "double-tap-gesture-handler",
      "force-press-gesture-handler",
      "horizontal-drag-gesture-handler",
      "long-press-gesture-handler",
      "pan-gesture-handler",
      "scale-gesture-handler",
      "tap-gesture-handler",
      "vertical-drag-gesture-handler",
      "text",
      "icon",
      "progress",
      "rich-text",
      "button",
      "form",
      "input",
      "textarea",
      "label",
      "checkbox",
      "checkbox-group",
      "radio",
      "radio-group",
      "switch",
      "slider",
      "picker",
      "picker-view",
      "picker-view-column",
      "editor",
      "editor-portal",
      "keyboard-accessory",
      "selection",
      "navigator",
      "functional-page-navigator",
      "image",
      "video",
      "audio",
      "camera",
      "live-player",
      "live-pusher",
      "voip-room",
      "channel-live",
      "channel-video",
      "map",
      "canvas",
      "web-view",
      "ad",
      "ad-custom",
      "official-account",
      "official-account-publish",
      "open-data",
      "open-data-item",
      "open-data-list",
      "native-component",
      "aria-component",
      "store-product",
      "store-home",
      "store-coupon",
      "store-gift",
    )
  }
}

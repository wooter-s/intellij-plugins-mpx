// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.codeInsight

import junit.framework.TestCase

/**
 * Unit tests for [WxEventNameUtils].
 *
 * Tests conversion between WeChat Mini Program event binding syntaxes:
 * - Merged syntax: `bindtap`, `catchtouchstart`, `capture-bindtap`
 * - Colon syntax: `bind:tap`, `catch:touchstart`, `capture-bind:tap`
 */
class WxEventNameUtilsTest : TestCase() {

  // ============ isWxEventAttribute tests ============

  fun testIsWxEventAttribute_bindMerged() {
    assertTrue(WxEventNameUtils.isWxEventAttribute("bindtap"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("bindtouchstart"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("bindabilityfail"))
  }

  fun testIsWxEventAttribute_bindColon() {
    assertTrue(WxEventNameUtils.isWxEventAttribute("bind:tap"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("bind:touchstart"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("bind:abilityfail"))
  }

  fun testIsWxEventAttribute_catchMerged() {
    assertTrue(WxEventNameUtils.isWxEventAttribute("catchtap"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("catchtouchstart"))
  }

  fun testIsWxEventAttribute_catchColon() {
    assertTrue(WxEventNameUtils.isWxEventAttribute("catch:tap"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("catch:touchstart"))
  }

  fun testIsWxEventAttribute_mutBindMerged() {
    assertTrue(WxEventNameUtils.isWxEventAttribute("mut-bindtap"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("mut-bindtouchstart"))
  }

  fun testIsWxEventAttribute_mutBindColon() {
    assertTrue(WxEventNameUtils.isWxEventAttribute("mut-bind:tap"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("mut-bind:touchstart"))
  }

  fun testIsWxEventAttribute_captureBindMerged() {
    assertTrue(WxEventNameUtils.isWxEventAttribute("capture-bindtap"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("capture-bindtouchstart"))
  }

  fun testIsWxEventAttribute_captureBindColon() {
    assertTrue(WxEventNameUtils.isWxEventAttribute("capture-bind:tap"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("capture-bind:touchstart"))
  }

  fun testIsWxEventAttribute_captureCatchMerged() {
    assertTrue(WxEventNameUtils.isWxEventAttribute("capture-catchtap"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("capture-catchtouchstart"))
  }

  fun testIsWxEventAttribute_captureCatchColon() {
    assertTrue(WxEventNameUtils.isWxEventAttribute("capture-catch:tap"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("capture-catch:touchstart"))
  }

  fun testIsWxEventAttribute_markAndModelAreNotEvents() {
    assertFalse(WxEventNameUtils.isWxEventAttribute("modelvalue"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("model:value"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("marktap"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("mark:tap"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("markers"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("marker-id"))
  }

  fun testIsWxEventAttribute_notEvent() {
    // Regular attributes
    assertFalse(WxEventNameUtils.isWxEventAttribute("class"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("style"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("id"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("data-id"))

    // Vue directives
    assertFalse(WxEventNameUtils.isWxEventAttribute("v-if"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("v-for"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("v-model"))

    // wx: directives (not event bindings)
    assertFalse(WxEventNameUtils.isWxEventAttribute("wx:if"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("wx:for"))
  }

  fun testIsWxEventAttribute_prefixOnly() {
    // Prefix only without event name should not be considered valid event attributes
    assertFalse(WxEventNameUtils.isWxEventAttribute("bind"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("catch"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("mut-bind"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("capture-bind"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("capture-catch"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("model"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("mark"))
  }

  fun testIsWxEventAttribute_colonPrefixWithoutEventName() {
    // Colon prefix only without event name - still valid
    assertTrue(WxEventNameUtils.isWxEventAttribute("bind:"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("catch:"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("mut-bind:"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("capture-bind:"))
    assertTrue(WxEventNameUtils.isWxEventAttribute("capture-catch:"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("model:"))
    assertFalse(WxEventNameUtils.isWxEventAttribute("mark:"))
  }

  // ============ toColonSyntax tests ============

  fun testToColonSyntax_bind() {
    assertEquals("bind:tap", WxEventNameUtils.toColonSyntax("bindtap"))
    assertEquals("bind:touchstart", WxEventNameUtils.toColonSyntax("bindtouchstart"))
    assertEquals("bind:abilityfail", WxEventNameUtils.toColonSyntax("bindabilityfail"))
  }

  fun testToColonSyntax_catch() {
    assertEquals("catch:tap", WxEventNameUtils.toColonSyntax("catchtap"))
    assertEquals("catch:touchstart", WxEventNameUtils.toColonSyntax("catchtouchstart"))
  }

  fun testToColonSyntax_mutBind() {
    assertEquals("mut-bind:tap", WxEventNameUtils.toColonSyntax("mut-bindtap"))
    assertEquals("mut-bind:touchstart", WxEventNameUtils.toColonSyntax("mut-bindtouchstart"))
  }

  fun testToColonSyntax_captureBind() {
    assertEquals("capture-bind:tap", WxEventNameUtils.toColonSyntax("capture-bindtap"))
    assertEquals("capture-bind:touchstart", WxEventNameUtils.toColonSyntax("capture-bindtouchstart"))
  }

  fun testToColonSyntax_captureCatch() {
    assertEquals("capture-catch:tap", WxEventNameUtils.toColonSyntax("capture-catchtap"))
    assertEquals("capture-catch:touchstart", WxEventNameUtils.toColonSyntax("capture-catchtouchstart"))
  }

  fun testToColonSyntax_model() {
    assertNull(WxEventNameUtils.toColonSyntax("modelvalue"))
    assertNull(WxEventNameUtils.toColonSyntax("modelchecked"))
  }

  fun testToColonSyntax_mark() {
    assertNull(WxEventNameUtils.toColonSyntax("marktap"))
    assertNull(WxEventNameUtils.toColonSyntax("markers"))
  }

  fun testToColonSyntax_alreadyColon() {
    // Already in colon syntax, should return null
    assertNull(WxEventNameUtils.toColonSyntax("bind:tap"))
    assertNull(WxEventNameUtils.toColonSyntax("catch:touchstart"))
    assertNull(WxEventNameUtils.toColonSyntax("capture-bind:tap"))
  }

  fun testToColonSyntax_notEvent() {
    assertNull(WxEventNameUtils.toColonSyntax("class"))
    assertNull(WxEventNameUtils.toColonSyntax("style"))
    assertNull(WxEventNameUtils.toColonSyntax("bind"))
  }

  // ============ toMergedSyntax tests ============

  fun testToMergedSyntax_bind() {
    assertEquals("bindtap", WxEventNameUtils.toMergedSyntax("bind:tap"))
    assertEquals("bindtouchstart", WxEventNameUtils.toMergedSyntax("bind:touchstart"))
    assertEquals("bindabilityfail", WxEventNameUtils.toMergedSyntax("bind:abilityfail"))
  }

  fun testToMergedSyntax_catch() {
    assertEquals("catchtap", WxEventNameUtils.toMergedSyntax("catch:tap"))
    assertEquals("catchtouchstart", WxEventNameUtils.toMergedSyntax("catch:touchstart"))
  }

  fun testToMergedSyntax_mutBind() {
    assertEquals("mut-bindtap", WxEventNameUtils.toMergedSyntax("mut-bind:tap"))
    assertEquals("mut-bindtouchstart", WxEventNameUtils.toMergedSyntax("mut-bind:touchstart"))
  }

  fun testToMergedSyntax_captureBind() {
    assertEquals("capture-bindtap", WxEventNameUtils.toMergedSyntax("capture-bind:tap"))
    assertEquals("capture-bindtouchstart", WxEventNameUtils.toMergedSyntax("capture-bind:touchstart"))
  }

  fun testToMergedSyntax_captureCatch() {
    assertEquals("capture-catchtap", WxEventNameUtils.toMergedSyntax("capture-catch:tap"))
    assertEquals("capture-catchtouchstart", WxEventNameUtils.toMergedSyntax("capture-catch:touchstart"))
  }

  fun testToMergedSyntax_model() {
    assertNull(WxEventNameUtils.toMergedSyntax("model:value"))
    assertNull(WxEventNameUtils.toMergedSyntax("model:checked"))
  }

  fun testToMergedSyntax_mark() {
    assertNull(WxEventNameUtils.toMergedSyntax("mark:tap"))
    assertNull(WxEventNameUtils.toMergedSyntax("mark:touchstart"))
  }

  fun testToMergedSyntax_alreadyMerged() {
    // Already in merged syntax, should return null
    assertNull(WxEventNameUtils.toMergedSyntax("bindtap"))
    assertNull(WxEventNameUtils.toMergedSyntax("catchtouchstart"))
    assertNull(WxEventNameUtils.toMergedSyntax("capture-bindtap"))
  }

  fun testToMergedSyntax_notEvent() {
    assertNull(WxEventNameUtils.toMergedSyntax("class"))
    assertNull(WxEventNameUtils.toMergedSyntax("style"))
    assertNull(WxEventNameUtils.toMergedSyntax("bind"))
  }

  // ============ getAllVariants tests ============

  fun testGetAllVariants_mergedFirst() {
    // Merged syntax should come first
    val variants1 = WxEventNameUtils.getAllVariants("bindtap")
    assertEquals(listOf("bindtap", "bind:tap"), variants1)

    val variants2 = WxEventNameUtils.getAllVariants("bind:tap")
    assertEquals(listOf("bindtap", "bind:tap"), variants2)
  }

  fun testGetAllVariants_allPrefixes() {
    assertEquals(listOf("catchtap", "catch:tap"), WxEventNameUtils.getAllVariants("catchtap"))
    assertEquals(listOf("mut-bindtap", "mut-bind:tap"), WxEventNameUtils.getAllVariants("mut-bindtap"))
    assertEquals(listOf("capture-bindtap", "capture-bind:tap"), WxEventNameUtils.getAllVariants("capture-bindtap"))
    assertEquals(listOf("capture-catchtap", "capture-catch:tap"), WxEventNameUtils.getAllVariants("capture-catchtap"))
    assertEquals(listOf("modelvalue"), WxEventNameUtils.getAllVariants("modelvalue"))
    assertEquals(listOf("marktap"), WxEventNameUtils.getAllVariants("marktap"))
  }

  fun testGetAllVariants_fromColonSyntax() {
    assertEquals(listOf("bindtap", "bind:tap"), WxEventNameUtils.getAllVariants("bind:tap"))
    assertEquals(listOf("catchtap", "catch:tap"), WxEventNameUtils.getAllVariants("catch:tap"))
    assertEquals(listOf("mut-bindtap", "mut-bind:tap"), WxEventNameUtils.getAllVariants("mut-bind:tap"))
    assertEquals(listOf("capture-bindtap", "capture-bind:tap"), WxEventNameUtils.getAllVariants("capture-bind:tap"))
    assertEquals(listOf("capture-catchtap", "capture-catch:tap"), WxEventNameUtils.getAllVariants("capture-catch:tap"))
    assertEquals(listOf("model:value"), WxEventNameUtils.getAllVariants("model:value"))
    assertEquals(listOf("mark:tap"), WxEventNameUtils.getAllVariants("mark:tap"))
  }

  fun testGetAllVariants_notEvent() {
    assertEquals(listOf("class"), WxEventNameUtils.getAllVariants("class"))
    assertEquals(listOf("style"), WxEventNameUtils.getAllVariants("style"))
    assertEquals(listOf("wx:if"), WxEventNameUtils.getAllVariants("wx:if"))
  }

  // ============ normalize tests ============

  fun testNormalize_colonToMerged() {
    assertEquals("bindtap", WxEventNameUtils.normalize("bind:tap"))
    assertEquals("catchtouchstart", WxEventNameUtils.normalize("catch:touchstart"))
    assertEquals("capture-bindtap", WxEventNameUtils.normalize("capture-bind:tap"))
  }

  fun testNormalize_mergedUnchanged() {
    assertEquals("bindtap", WxEventNameUtils.normalize("bindtap"))
    assertEquals("catchtouchstart", WxEventNameUtils.normalize("catchtouchstart"))
    assertEquals("capture-bindtap", WxEventNameUtils.normalize("capture-bindtap"))
  }

  fun testNormalize_notEvent() {
    assertEquals("class", WxEventNameUtils.normalize("class"))
    assertEquals("style", WxEventNameUtils.normalize("style"))
  }

  // ============ areEquivalent tests ============

  fun testAreEquivalent_sameEvent() {
    assertTrue(WxEventNameUtils.areEquivalent("bindtap", "bind:tap"))
    assertTrue(WxEventNameUtils.areEquivalent("bind:tap", "bindtap"))
    assertTrue(WxEventNameUtils.areEquivalent("bindtap", "bindtap"))
    assertTrue(WxEventNameUtils.areEquivalent("bind:tap", "bind:tap"))
  }

  fun testAreEquivalent_differentEvents() {
    assertFalse(WxEventNameUtils.areEquivalent("bindtap", "bindtouchstart"))
    assertFalse(WxEventNameUtils.areEquivalent("bindtap", "catchtap"))
    assertFalse(WxEventNameUtils.areEquivalent("bindtap", "bind:touchstart"))
  }

  fun testAreEquivalent_allPrefixes() {
    assertTrue(WxEventNameUtils.areEquivalent("catchtap", "catch:tap"))
    assertTrue(WxEventNameUtils.areEquivalent("mut-bindtap", "mut-bind:tap"))
    assertTrue(WxEventNameUtils.areEquivalent("capture-bindtap", "capture-bind:tap"))
    assertTrue(WxEventNameUtils.areEquivalent("capture-catchtap", "capture-catch:tap"))
    assertFalse(WxEventNameUtils.areEquivalent("modelvalue", "model:value"))
    assertFalse(WxEventNameUtils.areEquivalent("marktap", "mark:tap"))
  }

  fun testAreEquivalent_nonEvents() {
    assertTrue(WxEventNameUtils.areEquivalent("class", "class"))
    assertFalse(WxEventNameUtils.areEquivalent("class", "style"))
  }
}

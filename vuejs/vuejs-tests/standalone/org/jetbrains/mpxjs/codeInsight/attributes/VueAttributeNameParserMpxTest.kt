// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.codeInsight.attributes

import junit.framework.TestCase
import org.jetbrains.mpxjs.codeInsight.attributes.VueAttributeNameParser.VueAttributeKind.PLAIN
import org.jetbrains.mpxjs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveInfo
import org.jetbrains.mpxjs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveKind.ON

class VueAttributeNameParserMpxTest : TestCase() {

  fun testNativeAttributesStartingWithMarkOrModelStayPlain() {
    listOf("markers", "marker-id", "mark-key", "model-value").forEach { attributeName ->
      val info = VueAttributeNameParser.parse(attributeName)

      assertEquals(attributeName, info.name)
      assertEquals(PLAIN, info.kind)
      assertFalse("$attributeName must not inject its whole Mustache value as JavaScript", info.injectJS)
    }
  }

  fun testWechatEventBindingsRemainEventDirectives() {
    mapOf(
      "bindtap" to "tap",
      "bind:tap" to "tap",
      "capture-catchtouchmove" to "touchmove",
      "capture-catch:touchmove" to "touchmove",
    ).forEach { (attributeName, eventName) ->
      val info = VueAttributeNameParser.parse(attributeName)

      assertTrue("$attributeName must be parsed as a directive", info is VueDirectiveInfo)
      info as VueDirectiveInfo
      assertEquals(ON, info.directiveKind)
      assertEquals(eventName, info.arguments)
      assertTrue("$attributeName handler must be injected as JavaScript", info.injectJS)
    }
  }
}

// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang

import junit.framework.TestCase
import java.nio.file.Files
import java.nio.file.Path

class MpxPluginIdentityTest : TestCase() {

  fun testMainPluginDescriptorUsesMpxIdentity() {
    val descriptor = read("vuejs-plugin/resources/META-INF/plugin.xml")
    assertTextContains(descriptor, "<id>org.wooter.mpxjs</id>")
    assertTextContains(descriptor, "<name>Mpx.js</name>")
    assertTextContains(descriptor, "coordinate=\"npm:@mpxjs/core\"")
    assertFalse(descriptor.contains("intellij.vuejs."))
  }

  fun testBackendDescriptorRegistersMpxFileTypeAndLanguages() {
    val descriptor = read("vuejs-backend/resources/intellij.mpxjs.backend.xml")
    assertTextContains(descriptor, "fileType name=\"Mpx.js\" extensions=\"mpx\" language=\"Mpx\"")
    assertTextContains(descriptor, "language=\"MpxJS\"")
    assertTextContains(descriptor, "language=\"MpxTS\"")
    assertTextContains(descriptor, "key=\"mpx.language.server.bundled.enabled\"")
    assertFalse(descriptor.contains("org.jetbrains.vuejs"))
  }

  fun testPluginTargetsTheWebStorm262Runtime() {
    val build = read("build.gradle.kts")
    assertTextContains(build, "sinceBuild = \"262.8665\"")
    assertTextContains(build, "untilBuild = \"262.*\"")
    assertTextContains(build, "jvmTarget.set(JvmTarget.JVM_25)")

    val properties = read("gradle.properties")
    assertTextContains(properties, "mpx.platformVersion=2026.2.0.1")
    assertTextContains(properties, "mpx.pluginVersion=262.8665.341.01")
  }

  fun testGeneratedWebTypesUseMpxFramework() {
    val webTypes = read("vuejs-backend/resources/web-types/vue@3.4.0.web-types.json")
    assertTextContains(webTypes, "\"framework\": \"mpx\"")
    assertTextContains(webTypes, "\"name\": \"mpx\"")
    assertTextContains(webTypes, "\"default-icon\": \"icons/mpx.svg\"")
    assertTextContains(webTypes, "\"wx-events\"")
  }

  fun testCreateActionUsesMpxTemplatesAndIsolatedPersistentIds() {
    val handler = read("vuejs-backend/src/org/jetbrains/mpxjs/VueCreateFromTemplateHandler.kt")
    assertTextContains(handler, "\"Mpx Class API Component\"")
    assertTextContains(handler, "\"Mpx Composition API Component\"")
    assertTextContains(handler, "\"Mpx Options API Component\"")
    assertTextContains(handler, "\"mpx.recent.templates\"")
    assertFalse(handler.contains("\"Vue Class API Component\""))

    val backendDescriptor = read("vuejs-backend/resources/intellij.mpxjs.backend.xml")
    assertTextContains(backendDescriptor, "notificationGroup")
    assertTextContains(backendDescriptor, "id=\"Mpx\"")

    listOf(
      "Mpx Class API Component.mpx.ft",
      "Mpx Composition API Component.mpx.ft",
      "Mpx Composition API Page.mpx.ft",
      "Mpx Options API Component.mpx.ft",
    ).forEach {
      val template = pluginRoot().resolve("vuejs-backend/resources/fileTemplates/internal").resolve(it)
      assertTrue("Expected MPX template exists: $template", Files.isRegularFile(template))
    }
  }

  private fun assertTextContains(text: String, expected: String) {
    assertTrue("Expected text to contain: $expected", text.contains(expected))
  }

  private fun read(relativePath: String): String {
    val file = pluginRoot().resolve(relativePath)
    assertTrue("Expected file exists: $file", Files.isRegularFile(file))
    return Files.readString(file)
  }

  private fun pluginRoot(): Path =
    Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize()
}

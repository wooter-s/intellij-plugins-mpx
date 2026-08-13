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
    assertTextContains(descriptor, "<content>")
    assertFalse(descriptor.contains("<content namespace="))
    assertFalse(descriptor.contains("intellij.vuejs."))
  }

  fun testBackendDescriptorRegistersMpxFileTypeAndLanguages() {
    val descriptor = read("vuejs-backend/resources/intellij.mpxjs.backend.xml")
    assertTextContains(descriptor, "fileType name=\"Mpx.js\" extensions=\"mpx\" language=\"Mpx\"")
    assertTextContains(descriptor, "language=\"MpxJS\"")
    assertTextContains(descriptor, "language=\"MpxTS\"")
    assertTextContains(descriptor, "key=\"mpx.language.server.bundled.enabled\"")
    assertTextContains(descriptor, "<plugin id=\"JavaScript\"/>")
    assertTextContains(descriptor, "<plugin id=\"org.intellij.plugins.postcss\"/>")
    assertFalse(descriptor.contains("<module name=\"intellij.javascript.ultimate\"/>"))
    assertFalse(descriptor.contains("<module name=\"intellij.postcss.paid\"/>"))
    assertFalse(descriptor.contains("org.jetbrains.vuejs"))

    val commonDescriptor = read("vuejs-common/resources/intellij.mpxjs.common.xml")
    assertTextContains(commonDescriptor, "<module name=\"intellij.css.common.psi\"/>")
  }

  fun testBackendDoesNotLinkPrivatePlatformModules() {
    val forbiddenImports = listOf(
      "com.intellij.lang.javascript.frameworks.jsx.JSXReferenceContributor",
      "org.intellij.plugins.postcss.PostCssCommentProvider",
      "org.intellij.plugins.postcss.PostCssLanguage",
      "org.intellij.plugins.postcss.styleLanguageOrPostCss",
    )
    val sourceRoot = pluginRoot().resolve("vuejs-backend/src")
    Files.walk(sourceRoot).use { paths ->
      paths
        .filter { Files.isRegularFile(it) && (it.toString().endsWith(".kt") || it.toString().endsWith(".java")) }
        .forEach { file ->
          val source = Files.readString(file)
          forbiddenImports.forEach { forbiddenImport ->
            assertFalse(
              "Production source must not import private platform type $forbiddenImport: $file",
              source.lineSequence().any { it.trim() == "import $forbiddenImport" },
            )
          }
        }
    }

    val lspService = read(
      "vuejs-backend/src/org/jetbrains/mpxjs/lang/typescript/service/lsp/VueLspTakeoverModeTypeScriptService.kt"
    )
    assertFalse(lspService.contains("override val diagnosticsConfiguration"))
    assertFalse(lspService.contains("PublishDiagnostics"))
  }

  fun testPluginTargetsTheWebStorm262Runtime() {
    val build = read("build.gradle.kts")
    assertTextContains(build, "sinceBuild = \"262.9437\"")
    assertTextContains(build, "untilBuild = \"262.*\"")
    assertTextContains(build, "jvmTarget.set(JvmTarget.JVM_25)")

    val properties = read("gradle.properties")
    assertTextContains(properties, "mpx.platformVersion=2026.2.1")
    assertTextContains(properties, "mpx.pluginVersion=262.9437.145.03")
  }

  fun testGeneratedWebTypesKeepMpxAndWxIdentitiesSeparate() {
    val mpxWebTypes = read("vuejs-backend/resources/web-types/vue@3.4.0.web-types.json")
    assertTextContains(mpxWebTypes, "\"framework\": \"mpx\"")
    assertTextContains(mpxWebTypes, "\"name\": \"mpx\"")
    assertTextContains(mpxWebTypes, "\"default-icon\": \"icons/mpx.svg\"")
    assertTextContains(mpxWebTypes, "\"wx-directives\"")
    assertTextContains(mpxWebTypes, "\"items\": \"/js/events\"")
    assertTextContains(mpxWebTypes, "\"vue-file-top-elements\"")
    assertTextContains(mpxWebTypes, "\"name\": \"qs\"")
    assertFalse(mpxWebTypes.contains("\"name\": \"bind alias\""))

    val wxWebTypes = read("vuejs-backend/resources/web-types/wx@0.0.0.web-types.json")
    assertFalse(wxWebTypes.contains("\"framework\":"))
    assertTextContains(wxWebTypes, "\"name\": \"wx\"")
    assertTextContains(wxWebTypes, "\"version\": \"0.0.0\"")
    assertTextContains(wxWebTypes, "\"kind\": \"framework\"")
    assertTextContains(wxWebTypes, "\"name\": \"mpx\"")
    assertTextContains(wxWebTypes, "\"default-icon\": \"icons/wx.svg\"")
    listOf("block", "import", "include", "template", "wxs", "page-meta", "navigation-bar", "text", "view").forEach {
      assertTextContains(wxWebTypes, "\"name\": \"$it\"")
    }

    val backendDescriptor = read("vuejs-backend/resources/intellij.mpxjs.backend.xml")
    assertTextContains(
      backendDescriptor,
      "<polySymbols.webTypes source=\"web-types/wx@0.0.0.web-types.json\" enableByDefault=\"true\"/>",
    )

    val wxIcon = read("vuejs-common/resources/icons/wx.svg")
    assertTextContains(wxIcon, "width=\"16\"")
    assertTextContains(
      read("vuejs-common/gen/org/jetbrains/mpxjs/VuejsIcons.java"),
      "Icon Wx = load(\"icons/wx.svg\"",
    )
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

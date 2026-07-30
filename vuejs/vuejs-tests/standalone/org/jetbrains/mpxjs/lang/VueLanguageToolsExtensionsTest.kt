package org.jetbrains.mpxjs.lang

import junit.framework.TestCase
import java.nio.file.Files
import java.nio.file.Path

class VueLanguageToolsExtensionsTest : TestCase() {

  fun testBundledTypeScriptPluginDefaultExtensionsContainMpx() {
    assertBundledFileContains(
      "vuejs-backend/vue-language-tools/typescript-plugin/3.0.11/node_modules/@vue/typescript-plugin/dist/index.js",
      "extensions:[\".mpx\"]",
    )
    assertBundledFileContains(
      "vuejs-backend/vue-language-tools/typescript-plugin/3.3.5/node_modules/@vue/typescript-plugin/dist/index.js",
      "extensions:[\".mpx\"]",
    )
  }

  fun testBundledLanguageServerDefaultExtensionsContainMpx() {
    listOf("2.2.10", "3.0.11", "3.3.5").forEach {
      assertBundledFileContains(
        "vuejs-backend/vue-language-tools/language-server/$it/bin/vue-language-server.js",
        "extensions: [\".mpx\"",
      )
    }
  }

  fun testBundledWebStormTypeScriptPluginRecognizesMpx() {
    assertBundledFileContains(
      "vuejs-backend/gen-resources/vue-service/node_modules/ws-typescript-vue-plugin/index.js",
      "\".mpx\"",
    )
  }

  private fun assertBundledFileContains(relativePath: String, substring: String) {
    val file = pluginRoot().resolve(relativePath)
    assertTrue("Expected file exists: $file", Files.isRegularFile(file))
    val text = Files.readString(file)
    assertTrue("Expected '$substring' in $file", text.contains(substring))
  }

  private fun pluginRoot(): Path =
    Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize()
}

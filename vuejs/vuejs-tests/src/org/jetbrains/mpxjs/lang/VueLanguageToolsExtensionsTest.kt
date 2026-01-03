package org.jetbrains.mpxjs.lang

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Files
import java.nio.file.Path

class VueLanguageToolsExtensionsTest : BasePlatformTestCase() {

  fun testBundledTypeScriptPluginDefaultExtensionsContainMpx() {
    assertBundledFileContains(
      "vuejs-backend/vue-language-tools/typescript-plugin/3.0.1/node_modules/@vue/typescript-plugin/index.js",
      "extensions: [\".mpx\"",
    )
    assertBundledFileContains(
      "vuejs-backend/vue-language-tools/typescript-plugin/3.1.2/node_modules/@vue/typescript-plugin/index.js",
      "extensions: [\".mpx\"",
    )
  }

  fun testBundledLanguageServerDefaultExtensionsContainMpx() {
    assertBundledFileContains(
      "vuejs-backend/vue-language-tools/language-server/2.2.10/bin/vue-language-server.js",
      "extensions: [\".mpx\"",
    )
  }

  private fun assertBundledFileContains(relativePath: String, substring: String) {
    val file = pluginRoot().resolve(relativePath)
    assertTrue("Expected file exists: $file", Files.isRegularFile(file))
    val text = Files.readString(file)
    assertTrue("Expected '$substring' in $file", text.contains(substring))
  }

  private fun pluginRoot(): Path {
    val testData = Path.of(getVueTestDataPath()).toAbsolutePath().normalize()
    return testData.parent.parent
  }
}


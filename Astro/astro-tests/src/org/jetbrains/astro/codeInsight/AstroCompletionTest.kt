package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.completion.JSLookupPriority
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.AstroTestModule

class AstroCompletionTest : AstroCodeInsightTestCase("codeInsight/completion") {

  fun testHtmlElements() = doLookupTest()
  fun testHtmlAttributes() = doLookupTest()
  fun testCharEntities() = doLookupTest()

  fun testScriptTagAttributes() =
    doLookupTest {
      it.priority > 0
    }

  fun testStyleTagAttributes() =
    doLookupTest {
      it.priority > 0
    }

  fun testImportedComponent() =
    doLookupTest(additionalFiles = listOf("component.astro"))

  fun testImportComponent() =
    doTypingTest("Compo\n", additionalFiles = listOf("component.astro"))

  fun testImportExternalSymbolFrontmatter() =
    doTypingTest("olo\n", additionalFiles = listOf("colors.ts"))

  fun testImportExternalSymbolExpression() =
    doTypingTest("olo\n", additionalFiles = listOf("colors.ts"))

  fun testImportWithinScriptBlock() =
    doTypingTest("getRandomNumber\n", additionalFiles = listOf("functions.ts"))

  fun testFrontmatterKeywords() =
    doLookupTest(additionalFiles = listOf("component.astro")) {
      it.priority.toInt() == JSLookupPriority.KEYWORDS_PRIORITY.priorityValue
      || it.priority.toInt() == JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
    }

  fun testPropsInterface() =
    doLookupTest(AstroTestModule.ASTRO_1_9_0)

  fun testTemplateLookupRoot() =
    doLookupTest()

  fun testTemplateLookupNestedHtml() =
    doLookupTest()

  fun testAstroComponentProps() =
    doLookupTest(additionalFiles = listOf("component.astro"))

  fun testAstroComponentProps2() =
    doLookupTest(additionalFiles = listOf("component.astro"))

  fun testAstroDirectives() =
    doLookupTest(additionalFiles = listOf("react-component.tsx"))

  fun testAstroDirectives2() =
    doLookupTest(additionalFiles = listOf("react-component.tsx"))

  // WEB-59265 only enabled completion at root level and nested in HTML but not as children of components.
  // This needs a fix before it can be enabled again.
  //fun testTemplateLookupNestedComponent() =
  //  doLookupTest(additionalFiles = listOf("component.astro"))

  //region Test configuration and helper methods

  private fun doTypingTest(textToType: String,
                           additionalFiles: List<String> = emptyList(),
                           vararg modules: AstroTestModule) {
    doConfiguredTest(additionalFiles = additionalFiles, modules = modules, checkResult = true) {
      completeBasic()
      type(textToType)
    }
  }

  //endregion

}
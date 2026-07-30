// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang

import org.jetbrains.mpxjs.lang.expr.VueJSParserTest
import org.jetbrains.mpxjs.lang.html.VueHighlightingLexerTest
import org.jetbrains.mpxjs.lang.html.VueIndexerTest
import org.jetbrains.mpxjs.lang.html.VueLexerTest
import org.jetbrains.mpxjs.lang.html.VueParserTest
import org.jetbrains.mpxjs.libraries.LibrariesTestSuite
import org.jetbrains.mpxjs.pug.PugTemplateTest
import org.jetbrains.mpxjs.pug.VuePugFoldingTest
import org.jetbrains.mpxjs.service.takeovermode.VueLspServiceDocumentationTest
import org.jetbrains.mpxjs.service.takeovermode.VueLspTypeScriptServiceTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  VueTestCommons::class,
  VueJSParserTest::class,
  VueLexerTest::class,
  VueHighlightingLexerTest::class,
  VueIndexerTest::class,
  VueParserTest::class,
  VueCompletionTest::class,
  VueCompletionTest.WithoutServiceTest::class,
  VueCommenterTest::class,
  VueHighlightingTest::class,
  VueHighlightingTest.WithoutServiceTest::class,
  VueControlFlowTest::class,
  VueComponentTest::class,
  VueNewComponentTest::class,
  VueTypedHandlerTest::class,
  VueAttributeNameParserTest::class,
  VueGotoDeclarationTest::class,
  VueFindUsagesTest::class,
  VueRenameTest::class,
  VueRenameTest.WithoutServiceTest::class,
  VueCssResolveTest::class,
  VueParameterInfoTest::class,
  VueOptimizeImportTest::class,
  VueLiveTemplatesTest::class,
  VueIntentionsTest::class,
  VueInjectionTest::class,
  VueFormatterTest::class,
  VueRearrangerTest::class,
  VueExternalFilesLinkingTest::class,
  VueExtractComponentTest::class,
  VueDocumentationTest::class,
  VueWebTypesDocumentationTest::class,
  VueCreateTsVariableTest::class,
  VueCopyrightTest::class,
  VueRefAttrsTest::class,
  VueAutoPopupTest::class,
  VueEmmetTest::class,
  VueTypeResolveTest::class,
  VueTemplateInjectionsTest::class,
  LibrariesTestSuite::class,
  PugTemplateTest::class,
  VuePugFoldingTest::class,
  VueModuleImportTest::class,
  VueCopyPasteTest::class,
  VueMoveTest::class,
  VueMoveModuleMemberTest::class,
  VueTypeScriptDuplicateTest::class,
  VueIntroduceVariableTest::class,
  VueTypeScriptLineMarkersTest::class,
  VueLspTypeScriptServiceTest::class,
  VueLspServiceDocumentationTest::class,
  VueIntegrationHighlightingTest::class,
  VueNpmIntegrationCompletionTest::class,
  VueYarnIntegrationCompletionTest::class,
  // Following tests are not working currently
  //VueESLintHighlightingTest::class,
  //VueTypeScriptWithTslintTest::class,
  //VueTypeScriptHighlightingTest::class,
)
class VueTestSuite

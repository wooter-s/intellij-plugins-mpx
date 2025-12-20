// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang

import org.jetbrains.mpxjs.lang.expr.VueJSParserTest
import org.jetbrains.mpxjs.lang.html.VueHighlightingLexerTest
import org.jetbrains.mpxjs.lang.html.VueIndexerTest
import org.jetbrains.mpxjs.lang.html.VueLexerTest
import org.jetbrains.mpxjs.lang.html.VueParserTest
import org.jetbrains.mpxjs.libraries.LibrariesTestSuite
import org.jetbrains.mpxjs.linters.tslint.VueESLintHighlightingTest
import org.jetbrains.mpxjs.linters.tslint.VueTypeScriptWithTslintTest
import org.jetbrains.mpxjs.pug.PugTemplateTest
import org.jetbrains.mpxjs.pug.VuePugFoldingTest
import org.jetbrains.mpxjs.service.VolarServiceDocumentationTest
import org.jetbrains.mpxjs.service.VolarServiceTest
import org.jetbrains.mpxjs.service.VueClassicTypeScriptServiceTest
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
  VueCommenterTest::class,
  VueHighlightingTest::class,
  VueControlFlowTest::class,
  VueComponentTest::class,
  VueNewComponentTest::class,
  VueTypedHandlerTest::class,
  VueAttributeNameParserTest::class,
  VueResolveTest::class,
  VueFindUsagesTest::class,
  VueRenameTest::class,
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
  LibrariesTestSuite::class,
  PugTemplateTest::class,
  VuePugFoldingTest::class,
  VueModuleImportTest::class,
  VueCopyPasteTest::class,
  VueMoveModuleMemberTest::class,
  VueTypeScriptDuplicateTest::class,
  VueIntroduceVariableTest::class,
  VueTypeScriptLineMarkersTest::class,
  VueESLintHighlightingTest::class,
  VueClassicTypeScriptServiceTest::class,
  VolarServiceTest::class,
  VolarServiceDocumentationTest::class,
  VueMoveTest::class,
  VueTypeScriptWithTslintTest::class,
  VueTypeScriptHighlightingTest::class,
  VueIntegrationHighlightingTest::class,
  VueNpmIntegrationCompletionTest::class,
  VueYarnIntegrationCompletionTest::class,
)
class VueTestSuite

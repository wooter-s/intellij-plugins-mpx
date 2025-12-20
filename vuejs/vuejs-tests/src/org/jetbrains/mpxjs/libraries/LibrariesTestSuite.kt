// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.libraries

import org.jetbrains.mpxjs.libraries.cssModules.CssModulesTest
import org.jetbrains.mpxjs.libraries.eslint.VueESLintImportCodeStyleTest
import org.jetbrains.mpxjs.libraries.i18n.I18nTest
import org.jetbrains.mpxjs.libraries.nuxt.NuxtTestSuite
import org.jetbrains.mpxjs.libraries.pinia.PiniaTest
import org.jetbrains.mpxjs.libraries.templateLoader.TemplateLoaderCompletionTest
import org.jetbrains.mpxjs.libraries.vueLoader.VueLoaderTest
import org.jetbrains.mpxjs.libraries.vuelidate.VuelidateTest
import org.jetbrains.mpxjs.libraries.vuex.VuexTestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  NuxtTestSuite::class,
  PiniaTest::class,
  VuexTestSuite::class,
  VueLoaderTest::class,
  TemplateLoaderCompletionTest::class,
  VuelidateTest::class,
  CssModulesTest::class,
  I18nTest::class,
  VueESLintImportCodeStyleTest::class
)
class LibrariesTestSuite

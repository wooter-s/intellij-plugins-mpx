// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.typescript.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import org.jetbrains.annotations.TestOnly

object VueServiceTestMixin {
  private const val FORCE_LEGACY_PLUGIN_USAGE_IN_TESTS_KEY = "vue.force.legacy.plugin.usage.in.tests"

  val forceLegacyPluginUsage: Boolean
    get() = ApplicationManager.getApplication().isUnitTestMode
            && System.getProperty(FORCE_LEGACY_PLUGIN_USAGE_IN_TESTS_KEY) == "force"

  @TestOnly
  fun setForceLegacyPluginUsage(
    value: Boolean,
    disposable: Disposable,
  ) {
    if (!value) {
      System.clearProperty(FORCE_LEGACY_PLUGIN_USAGE_IN_TESTS_KEY)
      return
    }

    System.setProperty(FORCE_LEGACY_PLUGIN_USAGE_IN_TESTS_KEY, "force")

    Disposer.register(disposable) {
      System.clearProperty(FORCE_LEGACY_PLUGIN_USAGE_IN_TESTS_KEY)
    }
  }
}

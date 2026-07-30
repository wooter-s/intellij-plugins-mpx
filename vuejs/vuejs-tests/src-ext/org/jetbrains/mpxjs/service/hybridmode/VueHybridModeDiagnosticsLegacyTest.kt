// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.service.hybridmode

import org.jetbrains.mpxjs.lang.VueTestModule
import org.jetbrains.mpxjs.lang.typescript.service.VueLanguageToolsVersion

class VueHybridModeDiagnosticsLegacyTest : VueHybridModeDiagnosticsTestBase(
  bundledVersion = VueLanguageToolsVersion.LEGACY,
) {
  override val vueTestModule: VueTestModule = VueTestModule.VUE_2_7_14
}

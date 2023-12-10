// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.codeInsight.imports

import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.JSImportCandidatesBase
import com.intellij.lang.javascript.modules.imports.providers.JSCandidatesProcessor
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import org.jetbrains.mpxjs.lang.html.VueFileType.Companion.isDotVueFile
import org.jetbrains.mpxjs.model.source.*
import java.util.function.Predicate

internal val SCRIPT_SETUP_API = setOf(
  DEFINE_PROPS_FUN,
  DEFINE_EMITS_FUN,
  DEFINE_EXPOSE_FUN,
  WITH_DEFAULTS_FUN,
  DEFINE_MODEL_FUN,
  DEFINE_OPTIONS_FUN,
  DEFINE_SLOTS_FUN,
)

class VueScriptCandidatesProvider(placeInfo: JSImportPlaceInfo) : JSImportCandidatesBase(placeInfo) {
  override fun getNames(keyFilter: Predicate<in String>): Set<String> {
    return SCRIPT_SETUP_API.filter { keyFilter.test(it) }.toSet()
  }

  override fun processCandidates(name: String, processor: JSCandidatesProcessor) {
    if (SCRIPT_SETUP_API.contains(name)) {
      processor.remove(name)
    }
  }
}

class VueScriptCandidatesProviderFactory : JSImportCandidatesProvider.CandidatesFactory {
  override fun createProvider(placeInfo: JSImportPlaceInfo): JSImportCandidatesProvider? {
    return if (placeInfo.file.isDotVueFile) VueScriptCandidatesProvider(placeInfo) else null
  }
}
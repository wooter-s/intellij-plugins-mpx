// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.options

import com.intellij.lang.typescript.lsp.createPackageRef
import com.intellij.lang.typescript.lsp.defaultPackageKey
import com.intellij.lang.typescript.lsp.extractRefText
import com.intellij.lang.typescript.lsp.restartTypeScriptServicesAsync
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import org.jetbrains.mpxjs.lang.typescript.service.volar.VolarExecutableDownloader

fun getVueSettings(project: Project): VueSettings = project.service<VueSettings>()

@Service(Service.Level.PROJECT)
@State(name = "VueSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class VueSettings(val project: Project) : SimplePersistentStateComponent<VueSettingsState>(VueSettingsState()) {

  var serviceType
    get() = state.innerServiceType
    set(value) {
      val changed = state.innerServiceType != value
      state.innerServiceType = value
      if (changed) restartTypeScriptServicesAsync(project)
    }

  var packageRef
    get() = createPackageRef(state.packageName, VolarExecutableDownloader.packageDescriptor.serverPackage)
    set(value) {
      val refText = extractRefText(value)
      val changed = state.packageName != refText
      state.packageName = refText
      if (changed) restartTypeScriptServicesAsync(project)
    }

}

class VueSettingsState : BaseState() {
  var innerServiceType by enum(VueServiceSettings.AUTO)
  var packageName by string(defaultPackageKey)
}

enum class VueServiceSettings {
  AUTO,
  VOLAR,
  TS_SERVICE,
  DISABLED
}
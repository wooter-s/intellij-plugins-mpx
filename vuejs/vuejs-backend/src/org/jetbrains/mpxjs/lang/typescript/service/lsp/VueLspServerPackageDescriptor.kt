// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.typescript.service.lsp

import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.lang.typescript.lsp.PackageVersion
import com.intellij.openapi.util.registry.Registry
import org.jetbrains.mpxjs.lang.typescript.service.vueLspPackageName
import org.jetbrains.mpxjs.lang.typescript.service.vuePluginPath

internal class VueLspServerPackageDescriptor(versionString: String) : LspServerPackageDescriptor(
  name = vueLspPackageName,
  defaultVersion = createVueLspPackageVersion(versionString),
  defaultPackageRelativePath = "/bin/vue-language-server.js",
) {
  override val registryVersion: String
    get() = Registry.stringValue("mpx.language.server.default.version")
}

private fun createVueLspPackageVersion(versionString: String): PackageVersion {
  return PackageVersion.bundled<VueLspServerPackageDescriptor>(
    version = versionString,
    pluginPath = vuePluginPath,
    localPath = "vue-language-tools/language-server/$versionString",
    isBundledEnabled = { Registry.`is`("mpx.language.server.bundled.enabled") },
  )
}
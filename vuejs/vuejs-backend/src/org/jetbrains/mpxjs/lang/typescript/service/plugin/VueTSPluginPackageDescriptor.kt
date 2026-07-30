// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.typescript.service.plugin

import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.lang.typescript.lsp.PackageVersion
import com.intellij.openapi.util.registry.Registry
import org.jetbrains.mpxjs.lang.typescript.service.vueTSPluginPackageName

internal class VueTSPluginPackageDescriptor(version: PackageVersion) : LspServerPackageDescriptor(
  name = vueTSPluginPackageName,
  defaultVersion = version,
  defaultPackageRelativePath = "",
) {
  override val registryVersion: String
    get() = Registry.stringValue("mpx.ts.plugin.default.version")
}

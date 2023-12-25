// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.typescript.service.volar

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.lsp.JSFrameworkLspServerDescriptor
import com.intellij.lang.typescript.lsp.LspServerDownloader
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.mpxjs.lang.typescript.service.isVolarEnabledAndAvailable
import org.jetbrains.mpxjs.lang.typescript.service.isVolarFileTypeAcceptable
import org.jetbrains.mpxjs.options.getVueSettings

private val volarLspServerPackageDescriptor: () -> LspServerPackageDescriptor = {
  LspServerPackageDescriptor("@vue/language-server",
                             Registry.stringValue("vue.language.server.default.version"),
                             "/bin/vue-language-server.js")
}

class VolarSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    if (isVolarEnabledAndAvailable(project, file)) {
      serverStarter.ensureServerStarted(VolarServerDescriptor(project))
    }
  }
}

class VolarServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, VolarExecutableDownloader, "Mpx") {
  override fun isSupportedFile(file: VirtualFile): Boolean = isVolarFileTypeAcceptable(file)
}

@ApiStatus.Experimental
object VolarExecutableDownloader : LspServerDownloader(volarLspServerPackageDescriptor()) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getVueSettings(project).packageRef
  }
}

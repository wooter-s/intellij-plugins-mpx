// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.options

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import org.jetbrains.annotations.TestOnly

@TestOnly
fun configureVueService(
  project: Project,
  disposable: Disposable,
  mode: VueLSMode,
) {
  val settings = VueSettings.instance(project)
  val oldMode = settings.serviceType
  settings.serviceType = mode

  Disposer.register(disposable) {
    settings.serviceType = oldMode
  }
}

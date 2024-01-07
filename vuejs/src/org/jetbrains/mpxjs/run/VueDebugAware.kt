// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.run

import com.intellij.javascript.debugger.JavaScriptDebugAwareBase
import org.jetbrains.mpxjs.lang.html.VueFileType

internal class VueDebugAware : JavaScriptDebugAwareBase(VueFileType.INSTANCE)

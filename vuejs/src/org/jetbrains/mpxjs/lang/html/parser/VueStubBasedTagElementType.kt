// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.html.parser

import com.intellij.psi.impl.source.html.HtmlStubBasedTagElementType
import org.jetbrains.mpxjs.lang.html.VueLanguage

open class VueStubBasedTagElementType(debugName: String) : HtmlStubBasedTagElementType(debugName, VueLanguage.INSTANCE)
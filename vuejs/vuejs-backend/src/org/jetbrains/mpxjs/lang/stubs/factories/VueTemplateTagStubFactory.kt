// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.stubs.factories

import com.intellij.psi.impl.source.xml.stub.XmlTagStubImpl
import org.jetbrains.mpxjs.lang.html.parser.VueElementTypes.TEMPLATE_TAG
import org.jetbrains.mpxjs.lang.html.psi.impl.VueTemplateTagImpl

internal class VueTemplateTagStubFactory : VueStubBasedTagStubFactory({ TEMPLATE_TAG }) {
  override fun createPsi(stub: XmlTagStubImpl) = VueTemplateTagImpl(stub, elementType)
}
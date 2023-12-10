// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.html.psi.impl

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.TypeScriptJSFunctionTypeImpl
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations
import com.intellij.lang.javascript.psi.types.recordImpl.CallSignatureImpl
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.elementType
import org.jetbrains.mpxjs.codeInsight.toAsset
import org.jetbrains.mpxjs.lang.LangMode
import org.jetbrains.mpxjs.lang.html.VueFile
import org.jetbrains.mpxjs.lang.html.VueFileElementType
import org.jetbrains.mpxjs.lang.html.lexer.VueLangModeMarkerElementType
import org.jetbrains.mpxjs.lang.html.stub.impl.VueFileStubImpl
import org.jetbrains.mpxjs.model.VueModelManager

class VueFileImpl(viewProvider: FileViewProvider) : HtmlFileImpl(viewProvider, VueFileElementType.INSTANCE), VueFile {
  override fun getStub(): VueFileStubImpl? = super.getStub() as VueFileStubImpl?

  override val langMode: LangMode
    get() {
      val stub = stub
      if (stub != null) {
        return stub.langMode
      }

      val astMarker = lastChild?.elementType
      return if (astMarker is VueLangModeMarkerElementType) astMarker.langMode else LangMode.DEFAULT
    }

  override fun getDefaultExportedName(): String = toAsset(FileUtil.getNameWithoutExtension(name), true)

  override fun buildModuleType(module: PsiElement): JSType? {
    if (module !is VueScriptSetupEmbeddedContentImpl) return null

    return CachedValuesManager.getCachedValue(module) {
      val returnType = VueModelManager.findEnclosingContainer(module).thisType.asRecordType()
      val returnTypeInstantiated = returnType.transformTypeHierarchy(TypeScriptTypeRelations.instantiationTransformer(false))
      val source = JSTypeSourceFactory.createTypeSource(module, true)
      val functionType = TypeScriptJSFunctionTypeImpl(source, emptyList(), emptyList(), null, returnTypeInstantiated)
      val recordType = JSRecordTypeImpl(source, listOf(CallSignatureImpl(true, functionType)))
      CachedValueProvider.Result.createSingleDependency(recordType, PsiModificationTracker.MODIFICATION_COUNT)
    }
  }
}

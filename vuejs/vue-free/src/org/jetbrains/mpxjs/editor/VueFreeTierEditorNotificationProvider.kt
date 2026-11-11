// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.editor

import com.intellij.platform.trialPromotion.common.PluginWithFreeTierEditorNotificationProvider
import com.intellij.polySymbols.context.PolyContext
import com.intellij.polySymbols.framework.PolySymbolFramework.Companion.KIND_FRAMEWORK
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.Nls
import org.jetbrains.mpxjs.VueBundle

internal class VueFreeTierEditorNotificationProvider : PluginWithFreeTierEditorNotificationProvider {

  @Nls
  override fun getNotificationMessage(file: PsiFile): String? =
    if (PolyContext.get(KIND_FRAMEWORK, file) == "vue" && isFileWithVueContent(file))
      VueBundle.message("vue.free.tier.notification.message")
    else
      null

  private fun isFileWithVueContent(file: PsiFile): Boolean =
    (file.name.endsWith(".ts") &&
     vueImportRegex.containsMatchIn(PsiDocumentManager.getInstance(file.project).getDocument(file)?.charsSequence ?: ""))
    || file.name.endsWith(".html")
    || file.name.endsWith(".vue")

}

private val vueImportRegex = Regex("'vue'|\"vue\"|\\.vue['\"]", RegexOption.MULTILINE)
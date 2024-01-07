// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.PsiSourcedWebSymbol
import com.intellij.xml.util.XmlTagUtil
import org.jetbrains.mpxjs.VueBundle
import org.jetbrains.mpxjs.codeInsight.toAsset
import org.jetbrains.mpxjs.inspections.quickfixes.VueImportComponentQuickFix
import org.jetbrains.mpxjs.model.VueModelVisitor
import org.jetbrains.mpxjs.web.VueWebSymbolsQueryConfigurator

class VueMissingComponentImportInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return object : XmlElementVisitor() {
      override fun visitXmlTag(tag: XmlTag) {
        val descriptor = tag.descriptor
        if (descriptor !is WebSymbolElementDescriptor)
          return

        val symbol = descriptor.symbol
        if (symbol !is PsiSourcedWebSymbol
            || symbol.properties[VueWebSymbolsQueryConfigurator.PROP_VUE_PROXIMITY] != VueModelVisitor.Proximity.OUT_OF_SCOPE
            || symbol.properties[VueWebSymbolsQueryConfigurator.PROP_VUE_COMPOSITION_COMPONENT] == true)
          return

        val elementToImport = symbol.source ?: return
        val tagNameToken = XmlTagUtil.getStartTagNameElement(tag) ?: return

        val tagName = tagNameToken.text
        holder.registerProblem(tagNameToken,
                               VueBundle.message("vue.inspection.message.missing.component.import", tagName),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                               VueImportComponentQuickFix(tagNameToken, toAsset(tagName, true), elementToImport))
      }
    }
  }
}

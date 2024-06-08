// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.inspections

import com.intellij.codeInsight.daemon.impl.analysis.RemoveTagIntentionFix
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import com.intellij.xml.util.HtmlUtil.TEMPLATE_TAG_NAME
import com.intellij.xml.util.XmlTagUtil
import org.jetbrains.mpxjs.VueBundle
import org.jetbrains.mpxjs.index.isScriptSetupTag
import org.jetbrains.mpxjs.lang.html.VueLanguage
import org.jetbrains.mpxjs.lang.html.isVueFile

class DuplicateTagInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return object : XmlElementVisitor() {
      override fun visitXmlTag(tag: XmlTag) {
        if (tag.language != VueLanguage.INSTANCE || !tag.containingFile.isVueFile) return
        val templateTag = TEMPLATE_TAG_NAME == tag.name
        if (!templateTag) return
        val parent = tag.parent as? XmlDocument ?: return
        if (parent.childrenOfType<XmlTag>().any {
            it != tag && (templateTag && it.name == TEMPLATE_TAG_NAME)
          }) {
          val tagName = XmlTagUtil.getStartTagNameElement(tag)
          holder.registerProblem(tagName ?: tag,
                                 VueBundle.message("vue.inspection.message.duplicate.tag", tag.name),
                                 RemoveTagIntentionFix(tag.name, tag))
        }
      }
    }
  }
}

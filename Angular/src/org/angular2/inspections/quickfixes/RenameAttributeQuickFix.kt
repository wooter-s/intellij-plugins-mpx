// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class RenameAttributeQuickFix(private val myNewAttributeName: String) : LocalQuickFix {

  @Nls
  override fun getName(): String {
    return Angular2Bundle.message("angular.quickfix.template.rename-attribute.name", myNewAttributeName)
  }

  @Nls
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.template.rename-attribute.family")
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val attribute = descriptor.psiElement as XmlAttribute
    attribute.setName(myNewAttributeName)
  }
}

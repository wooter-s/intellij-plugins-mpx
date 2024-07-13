// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInsight.daemon.impl.analysis.RemoveAttributeIntentionFix
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.util.startOffset
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.SmartList
import org.angular2.Angular2DecoratorUtil.EXPORT_AS_PROP
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.NG_EXPORT_AS
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.TS_PROPERTY
import org.angular2.codeInsight.Angular2HighlightingUtils.renderEntityList
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.parser.Angular2AttributeType

class AngularInvalidTemplateReferenceVariableInspection : AngularHtmlLikeTemplateLocalInspectionTool() {

  override fun visitAngularAttribute(holder: ProblemsHolder,
                                     attribute: XmlAttribute,
                                     descriptor: Angular2AttributeDescriptor) {
    val info = descriptor.info
    if (info.type == Angular2AttributeType.REFERENCE) {
      val valueElement = attribute.valueElement
      val exportName = valueElement?.value
      if (!exportName.isNullOrEmpty()) {
        val range = valueElement.valueTextRange.shiftLeft(valueElement.startOffset)
        val matchedDirectives = Angular2ApplicableDirectivesProvider(attribute.parent).matched
        val allMatching = matchedDirectives.filter { dir -> dir.exportAs.contains(exportName) }
        val scope = Angular2DeclarationsScope(attribute)
        val matching = allMatching.filter { d -> scope.contains(d) }
        if (matching.isEmpty()) {
          val quickFixes = SmartList<LocalQuickFix>()
          val proximity = scope.getDeclarationsProximity(allMatching)
          if (proximity != Angular2DeclarationsScope.DeclarationProximity.NOT_REACHABLE) {
            Angular2FixesFactory.addUnresolvedDeclarationFixes(attribute, quickFixes)
          }
          quickFixes.add(RemoveAttributeIntentionFix(attribute.name))
          holder.registerProblem(valueElement,
                                 Angular2Bundle.htmlMessage("angular.inspection.invalid-template-ref-var.message.unbound",
                                                            EXPORT_AS_PROP.withColor(NG_EXPORT_AS, attribute),
                                                            exportName.withColor(TS_PROPERTY, attribute)),
                                 Angular2InspectionUtils.getBaseProblemHighlightType(scope, matchedDirectives),
                                 range,
                                 *quickFixes.toTypedArray<LocalQuickFix>())
        }
        else if (matching.size > 1) {
          holder.registerProblem(
            valueElement, range,
            Angular2Bundle.htmlMessage("angular.inspection.invalid-template-ref-var.message.ambiguous-name",
                                       EXPORT_AS_PROP.withColor(NG_EXPORT_AS, attribute),
                                       exportName.withColor(TS_PROPERTY, attribute),
                                       renderEntityList(matching)))
        }
      }
    }
  }
}

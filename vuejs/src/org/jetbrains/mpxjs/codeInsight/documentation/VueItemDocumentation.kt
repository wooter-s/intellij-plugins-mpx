// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.codeInsight.documentation

import com.intellij.util.IncorrectOperationException
import org.jetbrains.annotations.Nls
import org.jetbrains.mpxjs.VueBundle
import org.jetbrains.mpxjs.model.*

interface VueItemDocumentation {
  /**
   * Default symbol name
   */
  val defaultName: String?

  /**
   * Symbol type
   */
  val type: String

  /**
   * Description of the entity with HTML markup
   */
  val description: String?

  /**
   * URL for external documentation
   */
  val docUrl: String?

  /**
   * Library of origin
   */
  val library: String?

  /**
   * Custom section to display in the documentation
   */
  val customSections: Map<String, String> get() = emptyMap()

  companion object {

    @Nls
    fun typeOf(item: VueDocumentedItem): String =
      when (item) {
        is VueFunctionComponent -> "mpx.documentation.type.functional.component"
        is VueComponent -> "mpx.documentation.type.component"
        is VueDirective -> "mpx.documentation.type.directive"
        is VueFilter -> "mpx.documentation.type.filter"
        is VueMethod -> "mpx.documentation.type.component.method"
        is VueEmitCall -> "mpx.documentation.type.component.event"
        is VueSlot -> "mpx.documentation.type.slot"
        is VueInputProperty -> "mpx.documentation.type.component.property"
        is VueComputedProperty -> "mpx.documentation.type.component.computed.property"
        is VueDataProperty -> "mpx.documentation.type.component.data.property"
        is VueDirectiveModifier -> "mpx.documentation.type.directive.modifier"
        is VueDirectiveArgument -> "mpx.documentation.type.directive.argument"
        else -> throw IncorrectOperationException(item.javaClass.name)
      }.let { VueBundle.message(it) }

    fun nameOf(item: VueDocumentedItem): String? =
      when (item) {
        is VueNamedEntity -> item.defaultName
        is VueNamedSymbol -> item.name
        is VueDirectiveArgument -> null
        else -> throw IncorrectOperationException(item.javaClass.name)
      }

  }
}

// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web

import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedName
import com.intellij.webSymbols.query.WebSymbolNamesProvider
import com.intellij.webSymbols.query.WebSymbolNamesProvider.Target.*
import org.jetbrains.mpxjs.VuejsIcons
import org.jetbrains.mpxjs.codeInsight.fromAsset
import org.jetbrains.mpxjs.codeInsight.toAsset
import org.jetbrains.mpxjs.lang.html.VueFileType
import org.jetbrains.mpxjs.lang.html.VueLanguage
import org.jetbrains.mpxjs.web.VueWebSymbolsQueryConfigurator.Companion.VUE_COMPONENTS
import org.jetbrains.mpxjs.web.VueWebSymbolsQueryConfigurator.Companion.VUE_COMPONENT_PROPS
import java.util.function.Predicate
import javax.swing.Icon

class VueFramework : WebFramework() {
  override val displayName: String = "Mpx"
  override val icon: Icon
    get() = VuejsIcons.Vue

  override fun isOwnTemplateLanguage(language: Language): Boolean =
    language.isKindOf(VueLanguage.INSTANCE)

  override fun getFileType(kind: SourceFileKind, context: VirtualFile, project: Project): WebFrameworkHtmlFileType? =
    when (kind) {
      SourceFileKind.HTML, SourceFileKind.STANDALONE -> VueFileType.INSTANCE
      else -> null
    }

  override fun getNames(qualifiedName: WebSymbolQualifiedName, target: WebSymbolNamesProvider.Target): List<String> {
    val name = qualifiedName.name

    return when (qualifiedName.qualifiedKind) {
      VUE_COMPONENTS -> when (target) {
        NAMES_QUERY -> listOf(name, fromAsset(name, true))
        NAMES_MAP_STORAGE -> if (name.contains('-'))
          listOf(name)
        else
          listOf(fromAsset(name, true))
        // TODO proposed variant should be taken from code style settings synced from ESLint settings
        CODE_COMPLETION_VARIANTS -> if (name.contains('-'))
          listOf(name)
        else
          listOf(name, fromAsset(name))
      }
      VUE_COMPONENT_PROPS -> when (target) {
        NAMES_QUERY -> listOf(fromAsset(name))
        NAMES_MAP_STORAGE -> listOf(fromAsset(name))
        CODE_COMPLETION_VARIANTS -> listOf(fromAsset(name))
      }
      WebSymbol.JS_EVENTS -> when (target) {
        NAMES_QUERY -> listOf(fromAsset(name), name, fromAsset(name, hyphenBeforeDigit = true))
        NAMES_MAP_STORAGE -> listOf(fromAsset(name, hyphenBeforeDigit = true))
        // TODO proposed variant should be taken from code style settings synced from ESLint settings
        CODE_COMPLETION_VARIANTS -> listOf(fromAsset(name), toAsset(name))
      }
      else -> emptyList()
    }
  }

  override fun getAttributeNameCodeCompletionFilter(tag: XmlTag): Predicate<String> =
    VueAttributeNameCodeCompletionFilter(tag)

  companion object {
    val instance get() = get("mpx")
    const val ID = "mpx"
  }
}
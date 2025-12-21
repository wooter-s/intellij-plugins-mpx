// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.web

import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.js.JS_EVENTS
import com.intellij.polySymbols.query.PolySymbolNamesProvider
import com.intellij.polySymbols.query.PolySymbolNamesProvider.Target.*
import com.intellij.psi.xml.XmlTag
import org.jetbrains.mpxjs.VuejsIcons
import org.jetbrains.mpxjs.codeInsight.fromAsset
import org.jetbrains.mpxjs.codeInsight.toAsset
import org.jetbrains.mpxjs.lang.html.VueFileType
import org.jetbrains.mpxjs.lang.html.VueLanguage
import java.util.function.Predicate
import javax.swing.Icon

class VueFramework : WebFramework() {
  override val displayName: String = "Vue"
  override val icon: Icon
    get() = VuejsIcons.Vue

  override fun isOwnTemplateLanguage(language: Language): Boolean =
    language.isKindOf(VueLanguage)

  override fun getFileType(kind: SourceFileKind, context: VirtualFile, project: Project): WebFrameworkHtmlFileType? =
    when (kind) {
      SourceFileKind.HTML, SourceFileKind.STANDALONE -> VueFileType
      else -> null
    }

  override fun getNames(qualifiedName: PolySymbolQualifiedName, target: PolySymbolNamesProvider.Target): List<String> {
    val name = qualifiedName.name

    return when (qualifiedName.qualifiedKind) {
      VUE_COMPONENTS -> when (target) {
        NAMES_QUERY, RENAME_QUERY -> listOf(name, fromAsset(name, true))
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
      VUE_COMPONENT_PROPS -> if (name.getOrNull(0)?.isUpperCase() == true)
        listOf(name)
      else
        when (target) {
          NAMES_QUERY, RENAME_QUERY -> listOf(fromAsset(name), toAsset(name), fromAsset(name, hyphenBeforeDigit = true))
          NAMES_MAP_STORAGE -> listOf(toAsset(name))
          CODE_COMPLETION_VARIANTS -> listOf(fromAsset(name), toAsset(name))
        }
      JS_EVENTS -> when (target) {
        NAMES_QUERY, RENAME_QUERY -> listOf(fromAsset(name), name, fromAsset(name, hyphenBeforeDigit = true))
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
    const val ID = "mpx"
  }
}
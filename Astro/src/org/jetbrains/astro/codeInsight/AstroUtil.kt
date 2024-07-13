// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely
import org.jetbrains.astro.lang.psi.AstroContentRoot
import org.jetbrains.astro.lang.psi.AstroFrontmatterScript

const val ASTRO_PKG = "astro"
const val ASTRO_GLOBAL_INTERFACE = "AstroGlobal"
const val ASTRO_IMPLICIT_OBJECT = "Astro"

const val ASTRO_INLINE_DIRECTIVE = "is:inline"
const val ASTRO_DEFINE_VARS_DIRECTIVE = "define:vars"

const val ASTRO_CONFIG_NAME = "astro.config"

val ASTRO_CONFIG_FILES = setOf(
  "$ASTRO_CONFIG_NAME.js",
  "$ASTRO_CONFIG_NAME.cjs",
  "$ASTRO_CONFIG_NAME.mjs",
  "$ASTRO_CONFIG_NAME.ts",
)

fun PsiElement.astroContentRoot(): AstroContentRoot? =
  containingFile?.astroContentRoot()

fun PsiElement.frontmatterScript(): AstroFrontmatterScript? =
  containingFile?.astroContentRoot()?.frontmatterScript()

fun PsiFile.astroContentRoot(): AstroContentRoot? =
  children.firstNotNullOfOrNull { it as? AstroContentRoot }

fun AstroContentRoot.frontmatterScript(): AstroFrontmatterScript? =
  children.firstNotNullOfOrNull { it as? AstroFrontmatterScript }

fun AstroFrontmatterScript.propsInterface(): TypeScriptInterface? =
  children.firstNotNullOfOrNull { child -> child.asSafely<TypeScriptInterface>()?.takeIf { it.name == "Props" } }


fun JSPsiNamedElementBase.resolveIfImportSpecifier(): JSPsiNamedElementBase =
  (this as? ES6ImportSpecifier)
    ?.multiResolve(false)
    ?.asSequence()
    ?.mapNotNull { it.takeIf { it.isValidResult }?.element as? JSPsiNamedElementBase }
    ?.firstOrNull()
  ?: this
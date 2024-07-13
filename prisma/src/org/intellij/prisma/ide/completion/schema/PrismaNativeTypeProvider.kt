package org.intellij.prisma.ide.completion.schema

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import org.intellij.prisma.PrismaIcons
import org.intellij.prisma.ide.completion.PrismaCompletionProvider
import org.intellij.prisma.ide.schema.types.PrismaNativeType
import org.intellij.prisma.ide.schema.types.PrismaNativeTypeConstructor
import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.types.unwrapType


object PrismaNativeTypeProvider : PrismaCompletionProvider() {
  override val pattern: ElementPattern<out PsiElement> = psiElement().withParent(
    psiElement(PrismaPathExpression::class.java)
      .withParent(PrismaFieldAttribute::class.java)
      .with("withQualifier") { el ->
        val file = el.containingFile as? PrismaFile ?: return@with false
        val qualifier = el.qualifier

        if (qualifier != null) {
          qualifier.text in file.metadata.datasources
        }
        else {
          false
        }
      }
  )

  override fun addCompletions(
    parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet,
  ) {
    val file = parameters.originalFile as? PrismaFile ?: return
    val pathExpression = parameters.position.parent.asSafely<PrismaPathExpression>() ?: return
    val datasourceName = pathExpression.qualifier?.text ?: return
    val datasourceType = file.metadata.datasources[datasourceName]?.type ?: return
    val constructors = PrismaNativeType.findConstructorsByType(datasourceType)
    val fieldType = parameters.position.parentOfType<PrismaFieldDeclaration>()?.type?.unwrapType() ?: return

    constructors
      .filter { fieldType in it.types }
      .map {
        LookupElementBuilder.create(it.name)
          .withIcon(PrismaIcons.ATTRIBUTE)
          .withTailText(it.tailText)
          .withInsertHandler(it.insertHandler)
      }
      .forEach { result.addElement(it) }
  }

  private val PrismaNativeTypeConstructor.tailText: String?
    get() = if (numberOfArgs > 0 || numberOfOptionalArgs > 0) {
      "()"
    }
    else null

  private val PrismaNativeTypeConstructor.insertHandler: InsertHandler<LookupElement>?
    get() = if (numberOfArgs > 0 || numberOfOptionalArgs > 0) {
      ParenthesesInsertHandler.WITH_PARAMETERS
    }
    else null
}
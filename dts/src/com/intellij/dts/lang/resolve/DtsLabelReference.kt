package com.intellij.dts.lang.resolve

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.DtsIcons
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.lang.psi.before
import com.intellij.dts.lang.psi.getDtsPresentableText
import com.intellij.dts.lang.stubs.DTS_NODE_LABEL_INDEX
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import com.intellij.util.Processors

private val refFollowSet = TokenSet.create(
  TokenType.WHITE_SPACE,
  DtsTypes.RANGL,         // end of cell array
  DtsTypes.LBRACE,        // start of node
  DtsTypes.LPAREN,        // start of expression
  DtsTypes.SEMICOLON,     // property end
  DtsTypes.COMMA,         // list delimiter
)

/**
 * Represents a reference to a label in a DTS file. If the reference is not used
 * as a property value, only labels that were declared before the reference are
 * valid. Valid example:
 *
 * / {
 *     label: node { };
 * };
 *
 * &label { };
 *
 * But if the label is used as property value the order does not matter, like in
 * this example:
 *
 * / {
 *     prop = &label;
 *
 *     label: node { };
 * };
 *
 * @param value whether the reference is used as a property value
 */
class DtsLabelReference(
  element: PsiElement,
  rangeInElement: TextRange,
  private val label: String,
  private val value: Boolean,
) : PsiPolyVariantReferenceBase<PsiElement>(element, rangeInElement, false) {
  class AutoPopup : TypedHandlerDelegate() {
    override fun checkAutoPopup(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
      if (file !is DtsFile || c != '&') return Result.CONTINUE

      val element = file.findElementAt(editor.caretModel.offset)
      if (element == null || element.elementType in refFollowSet) {
        AutoPopupController.getInstance(project).scheduleAutoPopup(editor, CompletionType.BASIC, null)
      }

      return Result.CONTINUE
    }
  }

  /**
   * Recursively collects all included files from the given PsiFile. Only
   * considers includes before the maxOffset. Can also deal with recursive
   * includes.
   */
  private fun collectFiles(collection: MutableSet<VirtualFile>, file: PsiFile, maxOffset: Int?) {
    if (file !is DtsFile) return

    for (include in file.dtsTopLevelIncludes.filter { it.before(maxOffset) }) {
      val includeFile = include.resolve(file) ?: continue

      if (collection.add(includeFile.originalFile.virtualFile)) {
        collectFiles(collection, includeFile, null)
      }
    }
  }

  /**
   * Creates two search scopes, on for the current file and one for all included
   * files. If value is false only includes before the label are included else
   * all includes are used.
   * @return A Pair of GlobalSearchScopes - `localScope` and `includeScope`.
   */
  private fun createScopes(): Pair<GlobalSearchScope, GlobalSearchScope> {
    val project = element.project

    val localScope = GlobalSearchScope.fileScope(project, element.containingFile.originalFile.virtualFile)

    val includeFiles = mutableSetOf<VirtualFile>()
    collectFiles(includeFiles, element.containingFile, if (value) null else element.startOffset)

    val includeScope = GlobalSearchScope.filesScope(project, includeFiles)

    return Pair(localScope, includeScope)
  }

  private fun stubGetElements(key: String, scope: GlobalSearchScope): Collection<DtsNode> {
    return StubIndex.getElements(
      DTS_NODE_LABEL_INDEX,
      key,
      element.project,
      scope,
      DtsNode::class.java
    )
  }

  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    val (localScope, includeScope) = createScopes()

    val localCandidates = stubGetElements(label, localScope)
    val includeCandidates = stubGetElements(label, includeScope)

    // if not value filter all labels after the reference
    val results = localCandidates.filter { value || it.startOffset < element.startOffset } + includeCandidates

    return results.map(::PsiElementResolveResult).toTypedArray()
  }

  override fun getVariants(): Array<Any> {
    val (localScope, includeScope) = createScopes()
    val combinedScope = localScope.union(includeScope)

    val variants = mutableListOf<LookupElementBuilder>()

    // get all labels in include and local scope
    val keys = mutableListOf<String>()
    StubIndex.getInstance().processAllKeys(
      DTS_NODE_LABEL_INDEX,
      Processors.cancelableCollectProcessor(keys),
      combinedScope,
    )
    for (key in keys) {
      val node = stubGetElements(key, combinedScope).firstOrNull()
      if (node == null) continue

      // filter all labels after the reference
      if (!value && localScope.contains(node.containingFile.virtualFile) && node.startOffset > element.startOffset) continue

      val lookup = LookupElementBuilder.create(key)
        .withTypeText(node.getDtsPresentableText())
        .withPsiElement(node)
        .withIcon(DtsIcons.Node)

      variants.add(lookup)
    }

    return variants.toTypedArray()
  }
}
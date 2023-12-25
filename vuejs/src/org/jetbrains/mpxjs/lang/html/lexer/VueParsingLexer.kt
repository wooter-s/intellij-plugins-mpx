// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.html.lexer

import com.intellij.lexer.DelegateLexer
import com.intellij.lexer.RestartableLexer
import com.intellij.lexer.TokenIterator
import com.intellij.psi.tree.IElementType
import org.jetbrains.mpxjs.lang.LangMode
import org.jetbrains.mpxjs.lang.html.parser.VueParsing

//VueParsingLexer类中的start方法和advance方法是实现插值表达式解析的关键部分。  在start方法中，首先设置了additionalState为BASE_LEXING，然后调用checkPendingLangMode方法。在checkPendingLangMode方法中，如果parentLangMode为null（表示不是嵌套的词法分析器），并且词法分析器已经完成了词法分析（即baseToken为null），那么就将additionalState设置为ADDITIONAL_TOKEN_LEXING，这表示将要开始解析插值表达式。  在advance方法中，如果additionalState为ADDITIONAL_TOKEN_LEXING，那么就将additionalState设置为ADDITIONAL_TOKEN_LEXED，这表示已经完成了插值表达式的解析。如果additionalState为BASE_LEXING，那么就调用super.advance()方法进行正常的词法分析，并在完成后调用checkPendingLangMode方法检查是否需要开始解析插值表达式。  在getTokenType方法中，如果additionalState为ADDITIONAL_TOKEN_LEXING，那么就返回插值表达式的词素类型，这是通过调用delegateLexer.lexedLangMode.astMarkerToken来实现的。  总的来说，VueParsingLexer类通过在词法分析的过程中检查和设置additionalState的状态，来实现插值表达式的解析。
/**
 * Emits zero-length [VueLangModeMarkerElementType] as a last element of the token stream
 * to be used by [VueParsing] unless [parentLangMode] is not null.
 */
class VueParsingLexer(private val delegateLexer: VueLexer, private val parentLangMode: LangMode? = null)
  : DelegateLexer(delegateLexer), RestartableLexer {

  private var additionalState = BASE_LEXING

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    additionalState = initialState and 0b11
    delegateLexer.lexedLangMode = parentLangMode ?: LangMode.values()[initialState shr SHIFT_1 and 0b11]
    super.start(buffer, startOffset, endOffset, initialState shr SHIFT_2)

    if (additionalState != BASE_LEXING) return
    checkPendingLangMode()
  }

  override fun getState(): Int {
    val delegateState = super.getState()
    val langModeState = delegateLexer.lexedLangMode.ordinal
    return (delegateState shl SHIFT_2) or (langModeState shl SHIFT_1) or additionalState
  }

  override fun advance() {
    if (additionalState == ADDITIONAL_TOKEN_LEXING) {
      additionalState = ADDITIONAL_TOKEN_LEXED
    }
    if (additionalState != BASE_LEXING) return

    super.advance()
    checkPendingLangMode()
  }

  private fun checkPendingLangMode() {
    if (parentLangMode != null) return // do not emit additional token for nested lexers

    val baseToken = super.getTokenType()
    if (baseToken == null) {
      // delegate lexer has just finished lexing

      if (delegateLexer.lexedLangMode == LangMode.PENDING) {
        delegateLexer.lexedLangMode = LangMode.NO_TS
      }

      additionalState = ADDITIONAL_TOKEN_LEXING
    }
  }

  override fun getTokenType(): IElementType? {
    if (additionalState == ADDITIONAL_TOKEN_LEXING) {
      return delegateLexer.lexedLangMode.astMarkerToken
    }

    return super.getTokenType()
  }

  val lexedLangMode: LangMode
    get() {
      if (parentLangMode != null) return parentLangMode

      if (additionalState != ADDITIONAL_TOKEN_LEXED) error("can't use lexedLangMode before lexing the whole sequence")
      return delegateLexer.lexedLangMode
    }

  override fun getStartState(): Int {
    return 0
  }

  override fun isRestartableState(state: Int): Boolean {
    return delegateLexer.isRestartableState(state shr SHIFT_2)
  }

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int, tokenIterator: TokenIterator?) {
    start(buffer, startOffset, endOffset, initialState)
  }

  companion object {
    private const val SHIFT_1 = 2
    private const val SHIFT_2 = SHIFT_1 + 2

    private const val BASE_LEXING = 0
    private const val ADDITIONAL_TOKEN_LEXING = 1
    private const val ADDITIONAL_TOKEN_LEXED = 2
  }
}
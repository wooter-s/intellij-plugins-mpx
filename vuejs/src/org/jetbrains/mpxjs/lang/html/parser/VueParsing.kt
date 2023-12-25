// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.html.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HtmlParsing
import com.intellij.lang.javascript.JSStubElementTypes.*
import com.intellij.openapi.util.KeyWithDefaultValue
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.ThreeState
import com.intellij.xml.util.HtmlUtil.*
import org.jetbrains.mpxjs.VueBundle
import org.jetbrains.mpxjs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.mpxjs.codeInsight.attributes.VueAttributeNameParser.VueAttributeKind.*
import org.jetbrains.mpxjs.lang.LangMode
import org.jetbrains.mpxjs.lang.VueScriptLangs
import org.jetbrains.mpxjs.lang.expr.parser.VueJSEmbeddedExprTokenType
import org.jetbrains.mpxjs.lang.html.lexer.VueLangModeMarkerElementType
import org.jetbrains.mpxjs.lang.html.lexer.VueLexer
import org.jetbrains.mpxjs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_END
import org.jetbrains.mpxjs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_START
import org.jetbrains.mpxjs.lang.html.parser.VueStubElementTypes.SCRIPT_SETUP_JS_EMBEDDED_CONTENT
import org.jetbrains.mpxjs.lang.html.parser.VueStubElementTypes.SCRIPT_SETUP_TS_EMBEDDED_CONTENT
import org.jetbrains.mpxjs.model.SLOT_TAG_NAME
import java.util.*

class VueParsing(builder: PsiBuilder) : HtmlParsing(builder) {
  private val langMode: LangMode = builder.getUserData(VueScriptLangs.LANG_MODE) ?: LangMode.DEFAULT
  private val htmlCompatMode: Boolean = HTML_COMPAT_MODE.get(builder)

  override fun hasCustomTagContent(): Boolean {
    return token() === INTERPOLATION_START
  }

  override fun hasCustomTopLevelContent(): Boolean {
    return token() === INTERPOLATION_START
  }

  override fun shouldContinueMainLoop(): Boolean {
    return super.shouldContinueMainLoop() && token() !is VueLangModeMarkerElementType
  }

  override fun parseDocument() {
    super.parseDocument()
    if (token() is VueLangModeMarkerElementType) {
      advance()
    }
  }

  override fun parseCustomTagContent(xmlText: PsiBuilder.Marker?): PsiBuilder.Marker? {
    var result = xmlText
    val tt = token()
    if (tt === INTERPOLATION_START) {
      result = if (!inVPreContext()) {
        terminateText(result)
      }
      else {
        startText(result)
      }
      val interpolation = mark()
      advance()
      if (token() is VueJSEmbeddedExprTokenType) {
        maybeRemapCurrentToken(token())
        advance()
      }
      if (!inVPreContext()) {
        if (token() === INTERPOLATION_END) {
          advance()
          interpolation.drop()
        }
        else {
          interpolation.error(VueBundle.message("vue.parser.message.unterminated.interpolation"))
        }
      }
      else {
        if (token() === INTERPOLATION_END) {
          advance()
        }
        interpolation.collapse(XmlTokenType.XML_DATA_CHARACTERS)
      }
    }
    return result
  }

  override fun maybeRemapCurrentToken(tokenType: IElementType) {
    when (tokenType) {
      is VueJSEmbeddedExprTokenType -> {
        if (inVPreContext())
          builder.remapCurrentToken(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
        else
          builder.remapCurrentToken(tokenType.copyWithLanguage(langMode))
      }
      MOD_TS_EMBEDDED_CONTENT -> {
        if (inScriptSetup())
          builder.remapCurrentToken(SCRIPT_SETUP_TS_EMBEDDED_CONTENT)
      }
      MOD_ES6_EMBEDDED_CONTENT, MOD_EMBEDDED_CONTENT -> {
        if (inScriptSetup())
          builder.remapCurrentToken(SCRIPT_SETUP_JS_EMBEDDED_CONTENT)
      }
    }
  }

  override fun parseCustomTopLevelContent(error: PsiBuilder.Marker?): PsiBuilder.Marker? {
    val result = flushError(error)
    terminateText(parseCustomTagContent(null))
    return result
  }

  override fun parseAttribute() {
    assert(token() === XmlTokenType.XML_NAME)
    val attr = mark()
    val tagName = peekTagInfo().normalizedName
    val attributeInfo = VueAttributeNameParser.parse(builder.tokenText!!, tagName, stackSize() == 1)
    advance()
    if (attributeInfo is VueAttributeNameParser.VueDirectiveInfo
        && attributeInfo.directiveKind == VueAttributeNameParser.VueDirectiveKind.PRE) {
      (peekTagInfo() as VueHtmlTagInfo).hasVPre = true
    }
    else if (attributeInfo.kind == SCRIPT_SETUP) {
      (peekTagInfo() as VueHtmlTagInfo).hasScriptSetup = true
    }
    //XML_EQ 是一个 IElementType 实例，代表 XML 中的等号（"="）。在 XML 中，等号用于分隔属性名和属性值。例如，在 <tag attribute="value"> 中，attribute 和 value 之间的等号就是 XML_EQ 所代表的。
    if (token() === XmlTokenType.XML_EQ) {
      advance()
      parseAttributeValue()
    }
    if (tagName.lowercase(Locale.US) == SLOT_TAG_NAME) {
      attr.done(VueStubElementTypes.STUBBED_ATTRIBUTE)
    } else {
      //val contentText = attr.toString()
      //if (contentText.contains("{{") && contentText.contains("}}")) {
      //  attr.done(VueJSEmbeddedExprTokenType.createInterpolationExpression(langMode, builder.project))
      //  return
      //}
      when (attributeInfo.kind) {
        TEMPLATE_SRC, SCRIPT_SRC, STYLE_SRC -> attr.done(VueStubElementTypes.SRC_ATTRIBUTE)
        SCRIPT_ID -> attr.done(VueStubElementTypes.SCRIPT_ID_ATTRIBUTE)
        SCRIPT_SETUP, SCRIPT_GENERIC, STYLE_MODULE -> attr.done(VueStubElementTypes.STUBBED_ATTRIBUTE)
        REF -> attr.done(VueStubElementTypes.REF_ATTRIBUTE)
        else -> attr.done(XmlElementType.XML_ATTRIBUTE)
      }
    }
  }

  override fun getHtmlTagElementType(info: HtmlTagInfo, tagLevel: Int): IElementType {
    val tagName = info.normalizedName.lowercase(Locale.US)
    if (tagName in ALWAYS_STUBBED_TAGS
        || (tagLevel == 1 && tagName in TOP_LEVEL_TAGS)) {
      return if (tagName == TEMPLATE_TAG_NAME) VueStubElementTypes.TEMPLATE_TAG else VueStubElementTypes.STUBBED_TAG
    }
    return super.getHtmlTagElementType(info, tagLevel)
  }

  override fun createHtmlTagInfo(originalTagName: String, startMarker: PsiBuilder.Marker): HtmlTagInfoImpl {
    return VueHtmlTagInfo(normalizeTagName(originalTagName), originalTagName, startMarker, inVPreContext())
  }

  override fun isSingleTag(tagInfo: HtmlTagInfo): Boolean {
    return (htmlCompatMode || !VueLexer.isPossiblyComponentTag(tagInfo.originalName))
           && super.isSingleTag(tagInfo)
  }

  override fun isEndTagRequired(tagInfo: HtmlTagInfo): Boolean =
    if (htmlCompatMode) super.isEndTagRequired(tagInfo) else true

  override fun canOpeningTagAutoClose(tagToClose: HtmlTagInfo, openingTag: HtmlTagInfo): ThreeState =
    if (htmlCompatMode) super.canOpeningTagAutoClose(tagToClose, openingTag) else ThreeState.NO

  override fun canClosingTagAutoClose(tagToClose: HtmlTagInfo, closingTag: String): Boolean =
    if (htmlCompatMode) super.canClosingTagAutoClose(tagToClose, closingTag) else false

  private fun inVPreContext(): Boolean {
    var result = false
    processStackItems {
      if (it is VueHtmlTagInfo) {
        result = it.hasVPre
        false
      }
      else true
    }
    return result
  }

  private fun inScriptSetup(): Boolean =
    (peekTagInfo() as? VueHtmlTagInfo)?.hasScriptSetup == true

  private inner class VueHtmlTagInfo(normalizedName: String,
                                     originalName: String,
                                     marker: PsiBuilder.Marker,
                                     var hasVPre: Boolean,
                                     var hasScriptSetup: Boolean = false)
    : HtmlTagInfoImpl(normalizedName, originalName, marker)

  companion object {
    val ALWAYS_STUBBED_TAGS: List<String> = listOf(SCRIPT_TAG_NAME, SLOT_TAG_NAME)
    val TOP_LEVEL_TAGS: List<String> = listOf(TEMPLATE_TAG_NAME, STYLE_TAG_NAME)
    val HTML_COMPAT_MODE: KeyWithDefaultValue<Boolean> = KeyWithDefaultValue.create("vue.html.compat.mode", true)
  }
}

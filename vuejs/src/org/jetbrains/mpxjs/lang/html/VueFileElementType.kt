// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang.html

import com.intellij.lang.ASTNode
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.PsiBuilderFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.StubBuilder
import com.intellij.psi.impl.source.tree.SharedImplUtil
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.xml.HtmlFileElementType
import org.jetbrains.mpxjs.lang.LangMode
import org.jetbrains.mpxjs.lang.VueScriptLangs
import org.jetbrains.mpxjs.lang.expr.parser.VueJSStubElementTypes
import org.jetbrains.mpxjs.lang.html.VueFileType.Companion.isDotVueFile
import org.jetbrains.mpxjs.lang.html.lexer.VueParsingLexer
import org.jetbrains.mpxjs.lang.html.parser.VueParserDefinition
import org.jetbrains.mpxjs.lang.html.parser.VueParsing
import org.jetbrains.mpxjs.lang.html.parser.VueStubElementTypes
import org.jetbrains.mpxjs.lang.html.stub.impl.VueFileStubImpl

class VueFileElementType : IStubFileElementType<VueFileStubImpl>("mpx", VueLanguage.INSTANCE) {
  companion object {
    @JvmStatic
    val INSTANCE: VueFileElementType = VueFileElementType()

    const val INJECTED_FILE_SUFFIX = ".#@injected@#.html"

    fun readDelimiters(fileName: String?): Pair<String, String>? {
      if (fileName == null || !fileName.endsWith(INJECTED_FILE_SUFFIX)) return null
      val endDot = fileName.length - INJECTED_FILE_SUFFIX.length
      val separatorDot = fileName.lastIndexOf('.', endDot - 1)
      val startDot = fileName.lastIndexOf('.', separatorDot - 1)
      if (endDot < 0 || startDot < 0 || separatorDot < 0) {
        return null
      }
      return Pair(fileName.substring(startDot + 1, separatorDot), fileName.substring(separatorDot + 1, endDot))
    }
  }

  override fun getStubVersion(): Int {
    return HtmlFileElementType.getHtmlStubVersion() + VueStubElementTypes.VERSION + VueJSStubElementTypes.STUB_VERSION
  }

  override fun getExternalId(): String {
    return "$language:$this"
  }

  override fun getBuilder(): StubBuilder {
    return object : DefaultStubBuilder() {
      override fun createStubForFile(file: PsiFile): StubElement<*> {
        return if (file is VueFile) VueFileStubImpl(file) else super.createStubForFile(file)
      }
    }
  }

  override fun serialize(stub: VueFileStubImpl, dataStream: StubOutputStream) {
    dataStream.writeName(stub.langMode.canonicalAttrValue)
  }

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): VueFileStubImpl {
    return VueFileStubImpl(LangMode.fromAttrValue(dataStream.readNameString()!!))
  }

  /**
   * `HTML_COMPAT_MODE`是一个键，用于在解析Vue.js文件时设置HTML兼容模式。当`HTML_COMPAT_MODE`为`true`时，插件会尽可能地兼容HTML的语法和特性。例如，它会允许自闭合的标签，如`<br />`。当`HTML_COMPAT_MODE`为`false`时，插件会严格按照Vue.js的语法规则来解析文件。
   *
   * 在`VueFileElementType.kt`文件中，`HTML_COMPAT_MODE`被用于设置`PsiBuilder`的用户数据。`PsiBuilder`是用于构建PSI树的类，用户数据是一种可以附加到`PsiBuilder`上的额外信息。
   *
   * 以下是一个Vue.js代码的示例，展示了`HTML_COMPAT_MODE`的用途：
   *
   * ```vue
   * <template>
   *   <br />
   * </template>
   *
   * <script>
   * export default {
   *   data() {
   *     return {
   *       message: 'Hello, Vue!'
   *     }
   *   }
   * }
   * </script>
   * ```
   *
   * 在这个示例中，`<br />`是一个自闭合的标签。如果`HTML_COMPAT_MODE`为`true`，那么这个标签就会被正确地解析；如果`HTML_COMPAT_MODE`为`false`，那么这个标签就可能会导致解析错误。
   */
  override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
    val delimiters = readDelimiters(SharedImplUtil.getContainingFile(chameleon).name)
    val languageForParser = getLanguageForParser(psi)
    // TODO support for custom delimiters - port to Angular and merge
    if (languageForParser === VueLanguage.INSTANCE) {
      val project = psi.project
      val htmlCompatMode = !psi.containingFile.isDotVueFile
      val lexer = VueParserDefinition.createLexer(project, delimiters, htmlCompatMode)
      val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, languageForParser, chameleon.chars)
      lexer as VueParsingLexer
      builder.putUserData(VueScriptLangs.LANG_MODE, lexer.lexedLangMode) // read in VueParsing
      builder.putUserData(VueParsing.HTML_COMPAT_MODE, htmlCompatMode)
      psi.putUserData(VueScriptLangs.LANG_MODE, lexer.lexedLangMode) // read in VueElementTypes
      val parser = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser)!!.createParser(project)
      val node = parser.parse(this, builder)

      return node.firstChildNode
    }

    return super.doParseContents(chameleon, psi)
  }
}

/*
 *    Copyright (c) [2019] [zxy]
 *    [wechat-miniprogram-plugin] is licensed under the Mulan PSL v1.
 *    You can use this software according to the terms and conditions of the Mulan PSL v1.
 *    You may obtain a copy of Mulan PSL v1 at:
 *       http://license.coscl.org.cn/MulanPSL
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR
 *    PURPOSE.
 *    See the Mulan PSL v1 for more details.
 *
 *
 *                      Mulan Permissive Software License，Version 1
 *
 *    Mulan Permissive Software License，Version 1 (Mulan PSL v1)
 *    August 2019 http://license.coscl.org.cn/MulanPSL
 *
 *    Your reproduction, use, modification and distribution of the Software shall be subject to Mulan PSL v1 (this License) with following terms and conditions:
 *
 *    0. Definition
 *
 *       Software means the program and related documents which are comprised of those Contribution and licensed under this License.
 *
 *       Contributor means the Individual or Legal Entity who licenses its copyrightable work under this License.
 *
 *       Legal Entity means the entity making a Contribution and all its Affiliates.
 *
 *       Affiliates means entities that control, or are controlled by, or are under common control with a party to this License, ‘control’ means direct or indirect ownership of at least fifty percent (50%) of the voting power, capital or other securities of controlled or commonly controlled entity.
 *
 *    Contribution means the copyrightable work licensed by a particular Contributor under this License.
 *
 *    1. Grant of Copyright License
 *
 *       Subject to the terms and conditions of this License, each Contributor hereby grants to you a perpetual, worldwide, royalty-free, non-exclusive, irrevocable copyright license to reproduce, use, modify, or distribute its Contribution, with modification or not.
 *
 *    2. Grant of Patent License
 *
 *       Subject to the terms and conditions of this License, each Contributor hereby grants to you a perpetual, worldwide, royalty-free, non-exclusive, irrevocable (except for revocation under this Section) patent license to make, have made, use, offer for sale, sell, import or otherwise transfer its Contribution where such patent license is only limited to the patent claims owned or controlled by such Contributor now or in future which will be necessarily infringed by its Contribution alone, or by combination of the Contribution with the Software to which the Contribution was contributed, excluding of any patent claims solely be infringed by your or others’ modification or other combinations. If you or your Affiliates directly or indirectly (including through an agent, patent licensee or assignee）, institute patent litigation (including a cross claim or counterclaim in a litigation) or other patent enforcement activities against any individual or entity by alleging that the Software or any Contribution in it infringes patents, then any patent license granted to you under this License for the Software shall terminate as of the date such litigation or activity is filed or taken.
 *
 *    3. No Trademark License
 *
 *       No trademark license is granted to use the trade names, trademarks, service marks, or product names of Contributor, except as required to fulfill notice requirements in section 4.
 *
 *    4. Distribution Restriction
 *
 *       You may distribute the Software in any medium with or without modification, whether in source or executable forms, provided that you provide recipients with a copy of this License and retain copyright, patent, trademark and disclaimer statements in the Software.
 *
 *    5. Disclaimer of Warranty and Limitation of Liability
 *
 *       The Software and Contribution in it are provided without warranties of any kind, either express or implied. In no event shall any Contributor or copyright holder be liable to you for any damages, including, but not limited to any direct, or indirect, special or consequential damages arising from your use or inability to use the Software or the Contribution in it, no matter how it’s caused or based on which legal theory, even if advised of the possibility of such damages.
 *
 *    End of the Terms and Conditions
 *
 *    How to apply the Mulan Permissive Software License，Version 1 (Mulan PSL v1) to your software
 *
 *       To apply the Mulan PSL v1 to your work, for easy identification by recipients, you are suggested to complete following three steps:
 *
 *       i. Fill in the blanks in following statement, including insert your software name, the year of the first publication of your software, and your name identified as the copyright owner;
 *       ii. Create a file named “LICENSE” which contains the whole context of this License in the first directory of your software package;
 *       iii. Attach the statement to the appropriate annotated syntax at the beginning of each source file.
 *
 *    Copyright (c) [2019] [name of copyright holder]
 *    [Software Name] is licensed under the Mulan PSL v1.
 *    You can use this software according to the terms and conditions of the Mulan PSL v1.
 *    You may obtain a copy of Mulan PSL v1 at:
 *       http://license.coscl.org.cn/MulanPSL
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR
 *    PURPOSE.
 *
 *    See the Mulan PSL v1 for more details.
 */

package org.jetbrains.mpxjs.lang.html.tag

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.html.NAMESPACE_HTML
import com.intellij.polySymbols.html.attributes.HtmlAttributeSymbolDescriptor
import com.intellij.polySymbols.html.attributes.HtmlAttributeSymbolInfo
import com.intellij.polySymbols.query.PolySymbolQueryExecutorFactory
import com.intellij.psi.xml.XmlElement
import com.intellij.util.asSafely
import com.intellij.openapi.util.io.FileUtilRt
import org.jetbrains.mpxjs.lang.html.VueFile
import org.jetbrains.mpxjs.model.VueComponent
import org.jetbrains.mpxjs.model.VueContainer
import org.jetbrains.mpxjs.model.VueInputProperty
import org.jetbrains.mpxjs.model.VueModelManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.mpxjs.model.source.PROPS_PROP
import org.jetbrains.mpxjs.model.source.VueComponents
import org.jetbrains.mpxjs.web.VUE_DIRECTIVE_ARGUMENT
import org.jetbrains.mpxjs.web.VUE_DIRECTIVE_MODIFIERS
import org.jetbrains.mpxjs.web.VUE_DIRECTIVES
import org.jetbrains.mpxjs.web.VUE_GLOBAL_DIRECTIVES
import org.jetbrains.mpxjs.web.VUE_MODEL
import org.jetbrains.mpxjs.web.VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * [https://developers.weixin.qq.com/miniprogram/dev/reference/api/Component.html]
 */
class CustomComponentDescriptor(
    private val tag: XmlTag,
    private val element: PsiElement,
) : BasicElementDescriptor() {

    private val descriptorName: String = when (element) {
        is JSProperty -> element.name ?: tag.name
        is JSLiteralExpression -> element.stringValue ?: tag.name
        else -> tag.name
    }

    override fun getDefaultValue(): String? {
        return null
    }

    override fun getName(context: PsiElement?): String = descriptorName

    override fun getName(): String = descriptorName

    override fun getElementsDescriptors(context: XmlTag?): Array<XmlElementDescriptor> = emptyArray()

    override fun getAttributesDescriptors(context: XmlTag?): Array<XmlAttributeDescriptor> =
        getCustomComponentAttributeDescriptors(this)

    override fun init(element: PsiElement?) {

    }

    override fun getDefaultName(): String = descriptorName


    override fun getQualifiedName(): String {
        return descriptorName
    }


    override fun getDeclaration(): PsiElement {
        return this.element
    }

    override fun getAttributeDescriptor(attributeName: String, context: XmlTag?): XmlAttributeDescriptor? {
        getCustomComponentAttributeDescriptors(this).find { it.name == attributeName }?.let { return it }

        val targetTag = context ?: tag

        if (isWeChatDirective(attributeName)) {
            resolveWebTypesDirectiveDescriptor(attributeName, targetTag)?.let { return it }
            // fall through to a permissive descriptor to avoid namespace errors when web-types lookup fails
            return AnyXmlAttributeDescriptor(attributeName)
        }

        return super.getAttributeDescriptor(attributeName, context) ?: AnyXmlAttributeDescriptor(attributeName)
    }

    private fun isWeChatDirective(attributeName: String): Boolean {
        val lowerName = attributeName.lowercase(Locale.US)
        if (lowerName.startsWith("wx:")) {
            return true
        }
        if (lowerName.startsWith("@")) {
            return true
        }

        val prefixes = listOf(
            "bind",
            "mut-bind",
            "catch",
            "capture-bind",
            "capture-catch",
            "mark",
            "model",
        )

        return prefixes.any { prefix ->
            when {
                lowerName == prefix -> true
                lowerName.startsWith("$prefix:") -> true
                lowerName.startsWith(prefix) && lowerName.length > prefix.length -> true
                else -> false
            }
        }
    }

    private fun resolveWebTypesDirectiveDescriptor(
        attributeName: String,
        contextTag: XmlTag,
    ): XmlAttributeDescriptor? {
        val executor = PolySymbolQueryExecutorFactory
            .getInstance(contextTag.project)
            .create(contextTag, false)

        for (candidateName in candidateDirectiveNames(attributeName)) {
            val symbol = WEB_TYPES_DIRECTIVE_KINDS
                .asSequence()
                .flatMap { kind ->
                    executor
                        .nameMatchQuery(kind, candidateName)
                        .run()
                        .asSequence()
                }
                .filterIsInstance<PolySymbol>()
                .firstOrNull()

            if (symbol != null) {
                val info = HtmlAttributeSymbolInfo.create(attributeName, executor, symbol, contextTag)
                return HtmlAttributeSymbolDescriptor(info, contextTag)
            }
        }

        return null
    }

    private fun candidateDirectiveNames(attributeName: String): Sequence<String> =
        sequence {
            yield(attributeName)
            val afterColon = attributeName.substringAfter(':', missingDelimiterValue = attributeName)
            if (afterColon.isNotEmpty() && afterColon != attributeName) {
                yield(afterColon)
            }
            when {
                attributeName.startsWith("wx:") -> {
                    yield(attributeName.removePrefix("wx:"))
                }
                attributeName.startsWith("bind:") -> {
                    yield(attributeName.removePrefix("bind:"))
                }
                attributeName.startsWith("catch:") -> {
                    yield(attributeName.removePrefix("catch:"))
                }
                attributeName.startsWith("capture-bind:") -> {
                    yield(attributeName.removePrefix("capture-bind:"))
                }
                attributeName.startsWith("capture-catch:") -> {
                    yield(attributeName.removePrefix("capture-catch:"))
                }
                attributeName.startsWith("model:") -> {
                    yield(attributeName.removePrefix("model:"))
                }
            }
        }.distinct()

    companion object {
        private val LOG: Logger = Logger.getInstance(CustomComponentDescriptor::class.java)
        private val WEB_TYPES_DIRECTIVE_KINDS: List<PolySymbolQualifiedKind> = listOf(
            VUE_DIRECTIVES,
            VUE_GLOBAL_DIRECTIVES,
            VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES,
            VUE_MODEL,
            VUE_DIRECTIVE_ARGUMENT,
            VUE_DIRECTIVE_MODIFIERS,
            PolySymbolQualifiedKind[NAMESPACE_HTML, "wx-directives"],
            PolySymbolQualifiedKind[NAMESPACE_HTML, "bind-directives"],
        )
        private val WARNED_KEYS: MutableSet<String> = ConcurrentHashMap.newKeySet()

        private fun logWarnOnce(key: String, message: String) {
            if (WARNED_KEYS.add(key)) {
                LOG.warn(message)
            }
        }
    }

    fun getCustomComponentAttributeDescriptors(
        customComponentDescriptor: CustomComponentDescriptor
    ): Array<XmlAttributeDescriptor> {
        val resolutionTargets = linkedSetOf<PsiElement>().apply {
            add(customComponentDescriptor.declaration)
            add(customComponentDescriptor.tag)
        }
        val trace = StringBuilder()

        resolutionTargets.forEach { target ->
            ComponentWxmlUtils.findCustomComponentDefinitionJsFile(target)?.let { jsFile ->
                val props = ComponentJsUtils.findPropertiesItems(jsFile)
                if (!props.isNullOrEmpty()) {
                    LOG.info("CustomComponentDescriptor: 组件 ${customComponentDescriptor.name} 属性解析命中 JS 文件 ${jsFile.virtualFile.path}")
                    return props.map { CustomComponentAttributeDescriptor(it) }.toTypedArray()
                }
                logWarnOnce(
                    key = "js-props-missing:${customComponentDescriptor.name}:${jsFile.virtualFile.path}",
                    message = "CustomComponentDescriptor: JS 文件 ${jsFile.virtualFile.path} 未包含属性定义，组件 ${customComponentDescriptor.name}",
                )
            } ?: trace.append("JS=null(${target.javaClass.simpleName}); ")
        }

        resolutionTargets.forEach { target ->
            ComponentWxmlUtils.findCustomComponentDefinitionVueFile(target)?.let { vueFile ->
                val descriptors = buildDescriptorsFromVueFile(vueFile)
                if (descriptors.isNotEmpty()) {
                    LOG.info("CustomComponentDescriptor: 组件 ${customComponentDescriptor.name} 属性解析命中 Vue 文件 ${vueFile.virtualFile.path}")
                    return descriptors.toTypedArray()
                }
                logWarnOnce(
                    key = "vue-props-missing:${customComponentDescriptor.name}:${vueFile.virtualFile.path}",
                    message = "CustomComponentDescriptor: Vue 文件 ${vueFile.virtualFile.path} 未包含属性定义，组件 ${customComponentDescriptor.name}",
                )
            } ?: trace.append("Vue=null(${target.javaClass.simpleName}); ")
        }

        logWarnOnce(
            key = "attribute-resolve-miss:${customComponentDescriptor.name}:${customComponentDescriptor.tag.containingFile.virtualFile?.path}",
            message = "CustomComponentDescriptor: 未解析到组件 ${customComponentDescriptor.name} 的属性信息 | ${trace.toString().ifEmpty { "" }}",
        )
        return emptyArray()
    }

    private fun buildDescriptorsFromVueFile(vueFile: VueFile): List<XmlAttributeDescriptor> {
        val component: VueComponent = VueModelManager.getComponent(vueFile) ?: run {
            LOG.warn("CustomComponentDescriptor: VueModelManager 未返回组件 ${vueFile.virtualFile.path}")
            return emptyList()
        }
        val container = component.asSafely<VueContainer>() ?: run {
            LOG.warn("CustomComponentDescriptor: 组件 ${component.defaultName ?: component::class.java.simpleName} 不是 VueContainer 类型")
            return emptyList()
        }
        val props: List<VueInputProperty> = container.props
        if (props.isEmpty()) {
            logWarnOnce(
                key = "container-props-empty:${vueFile.virtualFile.path}",
                message = "CustomComponentDescriptor: 组件 ${component.defaultName ?: component::class.java.simpleName} props 为空, " +
                    "componentClass=${component::class.java.name} vueFile=${vueFile.virtualFile.path}",
            )
            buildFallbackProps(vueFile)?.let { fallbackProps ->
                LOG.info("CustomComponentDescriptor: 使用 fallback props ${fallbackProps.map { it.name }}")
                return fallbackProps
            }
        }
        val descriptors = linkedMapOf<String, XmlAttributeDescriptor>()
        props.forEach { prop ->
            val jsProperty = prop.source
                ?.asSafely<JSProperty>()
                ?: prop.source
                    ?.asSafely<JSImplicitElement>()
                    ?.context
                    ?.asSafely<JSProperty>()
            if (jsProperty == null) {
                LOG.warn("CustomComponentDescriptor: 属性 ${prop.name} 无 JSProperty 源, source=${prop.source}")
            }
            val descriptor = jsProperty?.let { CustomComponentAttributeDescriptor(it) }
                ?: VueInputPropertyAttributeDescriptor(prop)
            descriptors[descriptor.name] = descriptor
        }
        return descriptors.values.toList()
    }

    private fun buildFallbackProps(vueFile: VueFile): List<XmlAttributeDescriptor>? {
        val descriptor = VueComponents.getSourceComponentDescriptor(vueFile)
        val searchRoots = linkedSetOf<PsiElement>()
        descriptor?.initializer?.let { searchRoots.add(it) }
        descriptor?.source?.let { searchRoots.add(it) }
        if (searchRoots.isEmpty()) {
            searchRoots.add(vueFile)
        }

        val propertyHolders = linkedSetOf<JSProperty>()
        searchRoots.forEach { root ->
            PsiTreeUtil.findChildrenOfType(root, JSProperty::class.java).forEach { property ->
                val name = property.name
                if (name != null && COMPONENT_PROP_OPTION_NAMES.contains(name)) {
                    propertyHolders.add(property)
                }
            }
        }
        if (propertyHolders.isEmpty()) {
            LOG.debug("CustomComponentDescriptor: 未找到 ${COMPONENT_PROP_OPTION_NAMES} 属性，searchRoots=${searchRoots.map { it::class.java.simpleName }} vueFile=${vueFile.virtualFile.path}")
        } else {
            LOG.debug(
                "CustomComponentDescriptor: fallback 捕获属性容器 ${propertyHolders.map { it.name }} vueFile=${vueFile.virtualFile.path}",
            )
        }
        if (propertyHolders.isEmpty()) {
            logWarnOnce(
                key = "fallback-no-holders:${vueFile.virtualFile.path}",
                message = "CustomComponentDescriptor: Vue 文件 ${vueFile.virtualFile.path} 未包含属性定义，组件 ${FileUtilRt.getNameWithoutExtension(vueFile.name)}",
            )
            return null
        }

        val descriptors = linkedMapOf<String, XmlAttributeDescriptor>()
        var anyHolderResolved = false
        propertyHolders.forEach { holder ->
            val literal = resolveComponentPropsLiteral(holder) ?: return@forEach
            anyHolderResolved = true
            literal.properties.forEach { prop ->
                val propName = prop.name ?: return@forEach
                descriptors.computeIfAbsent(propName) { CustomComponentAttributeDescriptor(prop) }
            }
        }
        if (anyHolderResolved) {
            LOG.debug(
                "CustomComponentDescriptor: fallback 解析到属性 ${descriptors.keys} vueFile=${vueFile.virtualFile.path}",
            )
        }
        if (descriptors.isNotEmpty()) {
            return descriptors.values.toList()
        }
        if (anyHolderResolved) {
            logWarnOnce(
                key = "fallback-empty:${vueFile.virtualFile.path}",
                message = "CustomComponentDescriptor: Vue 文件 ${vueFile.virtualFile.path} 未包含属性定义，组件 ${FileUtilRt.getNameWithoutExtension(vueFile.name)}",
            )
            return emptyList()
        }
        return null
    }

}

class VueInputPropertyAttributeDescriptor(
    private val vueInputProperty: VueInputProperty,
) : XmlAttributeDescriptor {

    override fun getDefaultValue(): String? = vueInputProperty.defaultValue

    override fun validateValue(context: XmlElement?, value: String?): String? = null

    override fun getName(context: PsiElement?): String = vueInputProperty.name

    override fun getName(): String = vueInputProperty.name

    override fun isRequired(): Boolean = vueInputProperty.required

    override fun hasIdRefType(): Boolean = false

    override fun init(element: PsiElement?) {}

    override fun isFixed(): Boolean = false

    override fun getDeclaration(): PsiElement? = vueInputProperty.source

    override fun isEnumerated(): Boolean = false

    override fun getEnumeratedValues(): Array<String>? = null

    override fun hasIdType(): Boolean = false
}

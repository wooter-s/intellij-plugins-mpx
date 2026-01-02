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

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.json.JsonLanguage
import com.intellij.json.psi.JsonObject
import com.intellij.lang.javascript.psi.JSDefinitionExpression
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlTagNameProvider
import org.jetbrains.mpxjs.index.findTopJsonScriptVueTap
import org.jetbrains.mpxjs.lang.html.VueLanguage


class VueTagNameProvider : XmlTagNameProvider {
    override fun addTagNameVariants(
            elements: MutableList<LookupElement>, tag: XmlTag, prefix: String
    ) {
        if (tag.language !is VueLanguage) {
            return
        }

        val xmlFile = tag.containingFile?.containingFile as? XmlFile ?: return
        val jsXmlTag = findTopJsonScriptVueTap(xmlFile) ?: return

        // 获取所有组件名称并创建 LookupElement
        val componentNames = getUsingComponentNames(jsXmlTag, tag)
        elements.addAll(componentNames.map { (name, path) ->
            LookupElementBuilder.create(name)
                .withIcon(AllIcons.Nodes.WebFolder)
                .withTypeText(path ?: "Mpx Component")
        })
    }

    /**
     * 从 JSON script 标签中提取 usingComponents 定义的组件名称列表
     * 复用 CustomElementDescriptorProvider 的组件查找逻辑
     */
    private fun getUsingComponentNames(jsXmlTag: XmlTag, contextTag: XmlTag): List<Pair<String, String?>> {
        val result = mutableListOf<Pair<String, String?>>()

        // 尝试解析 JSObjectLiteralExpression（JavaScript 格式）
        val jsObjectLiteralExpressions = PsiTreeUtil.findChildrenOfType(
            jsXmlTag,
            JSObjectLiteralExpression::class.java
        )

        if (jsObjectLiteralExpressions.isEmpty()) {
            // 尝试解析纯 JSON 格式
            val psiFile = PsiFileFactory.getInstance(contextTag.project)
                .createFileFromText(JsonLanguage.INSTANCE, jsXmlTag.value.text)

            // 查找 usingComponents 对象
            val jsonObjects = PsiTreeUtil.findChildrenOfType(psiFile, JsonObject::class.java)
            for (jsonObj in jsonObjects) {
                val usingComponentsProp = jsonObj.findProperty("usingComponents")
                val usingComponentsValue = usingComponentsProp?.value as? JsonObject
                usingComponentsValue?.propertyList?.forEach { prop ->
                    val name = prop.name
                    val path = (prop.value as? com.intellij.json.psi.JsonStringLiteral)?.value
                    if (name.isNotBlank()) {
                        result.add(Pair(name, path))
                    }
                }
            }
            return result
        }

        // 解析 JavaScript 格式的 usingComponents
        var usingComponents: JSObjectLiteralExpression? = null
        for (expression in jsObjectLiteralExpressions) {
            val moduleExportsProperty = expression.properties.find { it.name == "usingComponents" }
            if (moduleExportsProperty != null) {
                if (moduleExportsProperty.value is JSObjectLiteralExpression) {
                    usingComponents = moduleExportsProperty.value as? JSObjectLiteralExpression
                }
                if (moduleExportsProperty.value is JSReferenceExpression) {
                    // 查找 JSReferenceExpression 对应的变量定义
                    val jsReferenceExpression = moduleExportsProperty.value as? JSReferenceExpression
                    if (jsReferenceExpression != null) {
                        val resolvedObject = resolveReferenceToObjectLiteral(jsReferenceExpression, jsXmlTag)
                        if (resolvedObject != null) {
                            usingComponents = resolvedObject
                        }
                    }
                }
                break
            }
        }

        // 从 usingComponents 对象中提取组件名称
        usingComponents?.properties?.forEach { property ->
            val name = property.name
            val path = (property.value as? JSLiteralExpression)?.stringValue
            if (!name.isNullOrBlank()) {
                result.add(Pair(name, path))
            }
        }

        // 查找动态注册的组件 (例如: usingComponents['component-name'] = '...')
        val dynamicDefinitions = PsiTreeUtil.findChildrenOfType(
            jsXmlTag,
            JSDefinitionExpression::class.java
        )
        for (target in dynamicDefinitions) {
            if (target.firstChild is JSIndexedPropertyAccessExpression) {
                val literalExpr = PsiTreeUtil.findChildOfType(target, JSLiteralExpression::class.java)
                val componentName = literalExpr?.stringValue
                if (!componentName.isNullOrBlank() && result.none { it.first == componentName }) {
                    // 尝试获取组件路径
                    val assignmentValue = target.parent?.lastChild
                    val path = (assignmentValue as? JSLiteralExpression)?.stringValue
                    result.add(Pair(componentName, path))
                }
            }
        }

        return result
    }

    /**
     * 解析 JSReferenceExpression 找到它引用的变量初始化的 JSObjectLiteralExpression
     * 例如: const usingComponents = {...}; module.exports = { usingComponents }
     * 这里需要找到 usingComponents 变量的初始化值 {...}
     */
    private fun resolveReferenceToObjectLiteral(
        reference: JSReferenceExpression,
        scope: PsiElement
    ): JSObjectLiteralExpression? {
        val variableName = reference.referenceName ?: return null

        // 在 scope 中查找同名的变量声明
        val variables = PsiTreeUtil.findChildrenOfType(scope, JSVariable::class.java)
        val variable = variables.find { it.name == variableName }

        // 获取变量的初始化表达式
        return variable?.initializer as? JSObjectLiteralExpression
    }
}

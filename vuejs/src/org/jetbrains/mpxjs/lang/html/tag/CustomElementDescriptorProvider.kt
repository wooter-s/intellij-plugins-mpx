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

import com.intellij.json.JsonLanguage
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.javascript.psi.*
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlElementDescriptor
import org.jetbrains.mpxjs.index.findTopJsonScriptVueTap
import org.jetbrains.mpxjs.lang.html.VueLanguage

/**
 * 提供Wxml小程序自带组件以及自定义组件的标签描述
 */
class CustomElementDescriptorProvider : XmlElementDescriptorProvider {
    override fun getDescriptor(xmlTag: XmlTag): XmlElementDescriptor? {
        if (xmlTag.language !is VueLanguage) {
            return null
        }
        xmlTag.name.ifBlank { return null }

        xmlTag.containingFile?.containingFile?.let {
            if (it is XmlFile) {
                val jsXmlTag = findTopJsonScriptVueTap(it)
                jsXmlTag?.let {
                    /**
                     * const usingComponents = {
                     *   "home-map": "./components/home-map",
                     *   "home-card": "./components/home-card",
                     *   "law": "./components/law.mpx",
                     *   "privacy": "./components/privacy",
                     *   "privacy-pop-wx": "./components/wxPrivacy/privacy-pop",
                     *   "privacy-guide-wx": "./components/wxPrivacy/guide",
                     *   "scan-button": "@qxsrc/components/scan-button/index.mpx",
                     *   "user-icon": "./components/user-icon",
                     *   "affiliate-func": "./components/affiliate-func",
                     *   "map-toolbox": "@qxsrc/components/map/map-toolbox.mpx",
                     *   "navigation-bar": "@qxsrc/components/navigation-bar"
                     * }
                     *
                     * if (__mpx_env__ === 'development') {
                     *   usingComponents['toggle-env'] = '@qxsrc/components/toggle-env'
                     *   usingComponents['dev-tool'] = './components/dev-tool'
                     * }
                     *
                     * module.exports =
                     * {
                     *   "navigationStyle": "custom",
                     *   "navigationBarTitleText": "首页",
                     *   "usingComponents": usingComponents,
                     *   "disableScroll": true
                     * }
                     */
                    // 现在想获取到usingComponents的JSProperty 应该如何操作？
                    // 获取到script里的module.export = 的JSObjetLiteralExpression
                    val jsObjectLiteralExpressions = PsiTreeUtil.findChildrenOfType(
                        jsXmlTag,
                        JSObjectLiteralExpression::class.java,
                    )

                    if (jsObjectLiteralExpressions.isEmpty()) {

                        val psiFile = PsiFileFactory.getInstance(xmlTag.project).createFileFromText(JsonLanguage.INSTANCE, jsXmlTag.value.text)
                        PsiTreeUtil.findChildrenOfType(psiFile, JsonStringLiteral::class.java).find {
                            it?.value == xmlTag.name
                        }.let {
                            return it?.let { CustomComponentDescriptor(it) }
                        }
                    }

                    var usingComponents: JSObjectLiteralExpression? = null
                    for (expression in jsObjectLiteralExpressions) {
                        val moduleExportsProperty = expression.properties.find { it.name == "usingComponents" }
                        if (moduleExportsProperty != null) {
                            if (moduleExportsProperty.value is JSObjectLiteralExpression) {
                                usingComponents = moduleExportsProperty.value as? JSObjectLiteralExpression
                            }
                            if (moduleExportsProperty.value is JSReferenceExpression) {
                                // 查找JSReferenceExpression对应的JSProperty
                                val jsReferenceExpression = moduleExportsProperty.value as? JSReferenceExpression

                                // 查找到对应赋值给jsReferenceExpression的JSObjectLiteralExpression
                                val jsObjectLiteralExpression = PsiTreeUtil.findChildrenOfType(
                                    jsXmlTag,
                                    JSObjectLiteralExpression::class.java,
                                ).find {
                                    it?.parent?.firstChild?.text == jsReferenceExpression?.text
                                }
                                if (jsObjectLiteralExpression != null)
                                    usingComponents = jsObjectLiteralExpression
                            }
                            break
                        }
                    }

                    // 获取到module.export里的usingComponents的JSProperty
                    var property = usingComponents?.properties?.find { it.name == xmlTag.name }

                    if (property == null) {
                        val target = PsiTreeUtil.findChildrenOfType(
                            jsXmlTag,
                            JSDefinitionExpression::class.java,
                        ).find {
                            it.name == xmlTag.name
                        }

                        if (target?.firstChild is JSIndexedPropertyAccessExpression) {
                            var property = PsiTreeUtil.findChildOfType(
                                target,
                                JSLiteralExpression::class.java,
                            )
                            return property?.let {
                                CustomComponentDescriptor(property)
                            }
                        } else if (target?.firstChild is JSReferenceExpression) {
                            return target?.firstChild?.lastChild?.let {
                                CustomComponentDescriptor(it)
                            }
                        }
                        print(target)
                    }

                    return property?.let {
                        CustomComponentDescriptor(property)
                    }
                    //PsiTreeUtil.findFirstContext(
                    //    usingComponents,
                    //    true
                    //) {
                    //    it is com.intellij.lang.javascript.psi.JSProperty && it.name == "usingComponents"
                    //}

                }
            }
        }
        return null
    }

    //private fun findCustomComponentJsonProperty(xmlTag: XmlTag): JsonProperty? {
    //    val tagName = xmlTag.name
    //    val wxmlPsiFile = xmlTag.containingFile
    //    val jsonFile = RelateFileHolder.JSON.findFile(wxmlPsiFile.originalFile) as? JsonFile ?: return null
    //    // 找到usingComponents的配置
    //    val usingComponentItems = mutableListOf<JsonProperty>().apply {
    //        ComponentJsonUtils.getUsingComponentItems(jsonFile)?.let {
    //            this.addAll(it)
    //        }
    //        AppJsonUtils.findUsingComponentItems(xmlTag.project)?.let {
    //            this.addAll(it)
    //        }
    //    }
    //    return usingComponentItems.find {
    //        it.name == tagName
    //    }
    //}
}
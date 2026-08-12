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

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSLiteralExpressionImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil

object ComponentJsUtils {

    data class ComponentEvent(
        val name: String,
        val sources: List<JSLiteralExpression>,
    )

    /**
     * 获取js文件或嵌入式脚本中的所有properties字段
     * 支持 JSFile（.js 文件）和 JSEmbeddedContent（.mpx 文件的 <script> 内容）
     */
    fun findPropertiesItems(scriptElement: PsiElement): Array<out JSProperty>? {
        return findComponentApiOptionProperties(scriptElement)?.find {
            it.name == "properties"
        }?.let {
            (it.value as? JSObjectLiteralExpression)?.properties
        }
    }

    /**
     * 从组件配置中提取静态 triggerEvent('eventName') 调用。
     * 同名事件只产生一个补全符号，但保留所有导航目标。
     */
    fun findTriggerEventItems(scriptElement: PsiElement): List<ComponentEvent> {
        val manager = CachedValuesManager.getManager(scriptElement.project)
        return manager.getCachedValue(
            scriptElement,
            manager.getKeyForClass(TriggerEventCache::class.java),
            {
                CachedValueProvider.Result.create(
                    calculateTriggerEventItems(scriptElement),
                    PsiModificationTracker.MODIFICATION_COUNT,
                )
            },
            false,
        )
    }

    /**
     * 找到Component API或Page API的方法调用
     * 支持: Component(), Page(), createComponent(), createPage()
     * 同时支持 JSFile 和 JSEmbeddedContent
     */
    fun findComponentOrPageCallExpression(scriptElement: PsiElement): JSCallExpression? {
        return PsiTreeUtil.findChildrenOfType(scriptElement, JSCallExpression::class.java)
            .asSequence()
            .sortedBy { it.textOffset }
            .find { callExpression ->
                (callExpression.methodExpression as? JSReferenceExpression)?.referenceName in COMPONENT_API_NAMES
            }
    }

    fun findCallExpressionFirstObjectArg(callExpression: JSCallExpression): JSObjectLiteralExpression? {
        return callExpression.arguments.firstOrNull() as? JSObjectLiteralExpression
    }

    /**
     * 找到脚本元素中的第一个component API的选项列表
     * 支持: Component(), Page(), createComponent(), createPage()
     * 同时支持 JSFile 和 JSEmbeddedContent
     */
    private fun findComponentApiOptionProperties(scriptElement: PsiElement): Array<JSProperty>? {
        val callExpression = findComponentOrPageCallExpression(scriptElement) ?: return null
        return findCallExpressionFirstObjectArg(callExpression)?.properties
    }

    private fun calculateTriggerEventItems(scriptElement: PsiElement): List<ComponentEvent> {
        val componentCall = findComponentOrPageCallExpression(scriptElement) ?: return emptyList()
        val componentOptions = findCallExpressionFirstObjectArg(componentCall) ?: return emptyList()
        val result = linkedMapOf<String, MutableList<JSLiteralExpression>>()

        PsiTreeUtil.findChildrenOfType(componentOptions, JSCallExpression::class.java)
            .asSequence()
            .sortedBy { it.textOffset }
            .forEach { callExpression ->
                val reference = callExpression.methodExpression as? JSReferenceExpression ?: return@forEach
                if (reference.referenceName != TRIGGER_EVENT_METHOD) return@forEach

                val eventLiteral = callExpression.arguments.firstOrNull() as? JSLiteralExpression ?: return@forEach
                if (!eventLiteral.isQuotedLiteral) return@forEach
                val eventName = eventLiteral.stringValue?.takeIf { it.isNotBlank() } ?: return@forEach
                result.getOrPut(eventName) { mutableListOf() }.add(eventLiteral)
            }

        return result.map { (name, sources) -> ComponentEvent(name, sources) }
    }

    /**
     * 获取Component中的externalClasses配置
     * https://developers.weixin.qq.com/miniprogram/dev/framework/custom-component/wxml-wxss.html#%E5%A4%96%E9%83%A8%E6%A0%B7%E5%BC%8F%E7%B1%BB
     */
    fun findComponentExternalClasses(jsFile: JSFile): List<String>? {
        val options = findComponentOptions(jsFile)
        return (options?.findProperty("externalClasses")?.value as? JSArrayLiteralExpression)?.expressions?.mapNotNull {
            (it as? JSLiteralExpressionImpl)?.stringValue
        }
    }

    /**
     * 找到Component调用中的参数
     * Component(options)
     */
    private fun findComponentOptions(jsFile: JSFile): JSObjectLiteralExpression? {
        return jsFile.children.filterIsInstance(JSExpressionStatement::class.java).mapNotNull {
            it.firstChild as? JSCallExpression
        }.find { jsCallExpression ->
            jsCallExpression.children.any { it is JSReferenceExpression && it.text == "Component" }
        }?.let { jsCallExpression ->
            (jsCallExpression.argumentList?.children?.find { it is JSObjectLiteralExpression } as? JSObjectLiteralExpression)
        }
    }

    private class TriggerEventCache

    private const val TRIGGER_EVENT_METHOD = "triggerEvent"

    private val COMPONENT_API_NAMES = setOf(
        "Component",
        "Page",
        "createComponent",
        "createPage",
    )

}

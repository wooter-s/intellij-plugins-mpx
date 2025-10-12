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

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiFileReference
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.mpxjs.index.VUE_FILE_EXTENSION
import org.jetbrains.mpxjs.lang.html.VueFile
import com.intellij.openapi.diagnostic.Logger

object ComponentWxmlUtils {

    ///**
    // * 判断一个wxml属性是否是自定义组件配置的externalClasses
    // */
    //fun isExternalClassesAttribute(attribute: XmlAttribute): Boolean {
    //    return attribute.parent?.let { this.findCustomComponentDefinitionJsFile(it) }?.let {
    //        ComponentJsUtils.findComponentExternalClasses(it)?.contains(attribute.name)
    //    } == true
    //}

    /**
     * 获取一个WXMLTag作为自定义组件的wxml文件
     */
    fun findCustomComponentDefinitionWxmlFile(xmlTag: XmlTag): VueFile? {
        return this.findCustomComponentDefinitionFiles(xmlTag)?.find { it is VueFile } as? VueFile
    }

    /**
     * 获取一个WXMLTag作为自定义组件的js文件
     */
    fun findCustomComponentDefinitionJsFile(xmlTag: XmlTag): JSFile? {
        return this.findCustomComponentDefinitionFiles(xmlTag)?.find { it is JSFile } as? JSFile
    }

    fun findCustomComponentDefinitionJsFile(jsProperty: JSProperty): JSFile? {
        return this.findCustomComponentDefinitionFiles(jsProperty)?.find { it is JSFile } as? JSFile
    }

    fun findCustomComponentDefinitionJsFile(jsObjectLiteralExpression: JSObjectLiteralExpression): JSFile? {
        return this.findCustomComponentDefinitionFiles(jsObjectLiteralExpression)?.find { it is JSFile } as? JSFile
    }

    fun findCustomComponentDefinitionJsFile(psiElement: PsiElement): JSFile? {
        return this.findCustomComponentDefinitionFiles(psiElement)?.find { it is JSFile } as? JSFile
    }

    fun findCustomComponentDefinitionVueFile(psiElement: PsiElement): VueFile? {
        return this.findCustomComponentDefinitionFiles(psiElement)?.find { it is VueFile } as? VueFile
    }

    private fun findCustomComponentDefinitionFiles(xmlTag: XmlTag): List<PsiFile>? {
        // 先找到json文件的定义
        val componentNameJsonProperty = xmlTag.descriptor?.declaration as? JSProperty
        LOG.debug("ComponentWxmlUtils: xmlTag=${xmlTag.name} descriptorDecl=${componentNameJsonProperty?.javaClass}")
        // 解析其路径可以找到自定义组件的位置
        return componentNameJsonProperty?.let { this.findCustomComponentDefinitionFiles(it) }
    }

    private fun findCustomComponentDefinitionFiles(jsProperty: JSProperty): List<PsiFile>? {
        // 解析其路径可以找到自定义组件的位置
        val value = jsProperty.value
        LOG.debug("ComponentWxmlUtils: jsProperty=${jsProperty.name} value=${value?.javaClass}")
        val lastComponentPathReference = value?.references?.lastOrNull() as? PsiFileReference
        val resolvedByReference = lastComponentPathReference?.multiResolve(false)
            ?.mapNotNull { it.element as? PsiFile }
        if (!resolvedByReference.isNullOrEmpty()) {
            LOG.info("ComponentWxmlUtils: 通过 PSI 引用解析到 ${resolvedByReference.joinToString { it.virtualFile?.path ?: it.name }}")
            return resolvedByReference
        }
        return resolveFromLiteral(value)
    }

    private fun findCustomComponentDefinitionFiles(jsObjectLiteralExpression: JSObjectLiteralExpression): List<PsiFile>? {
        // 解析其路径可以找到自定义组件的位置
        val lastComponentPathReference = jsObjectLiteralExpression?.references?.lastOrNull() as? PsiFileReference
        val resolvedByReference = lastComponentPathReference?.multiResolve(false)
            ?.mapNotNull { it.element as? PsiFile }
        if (!resolvedByReference.isNullOrEmpty()) {
            LOG.info("ComponentWxmlUtils: 通过 PSI 引用解析到 ${resolvedByReference.joinToString { it.virtualFile?.path ?: it.name }}")
            return resolvedByReference
        }
        return resolveFromLiteral(jsObjectLiteralExpression)
    }

    private fun findCustomComponentDefinitionFiles(psiElement: PsiElement): List<PsiFile>? {
        // 解析其路径可以找到自定义组件的位置
        val lastComponentPathReference = psiElement?.references?.lastOrNull() as? PsiFileReference
        val resolvedByReference = lastComponentPathReference?.multiResolve(false)
            ?.mapNotNull { it.element as? PsiFile }
        if (!resolvedByReference.isNullOrEmpty()) {
            LOG.info("ComponentWxmlUtils: 通过 PSI 引用解析到 ${resolvedByReference.joinToString { it.virtualFile?.path ?: it.name }}")
            return resolvedByReference
        }
        return resolveFromLiteral(psiElement)
    }

    private fun resolveFromLiteral(element: PsiElement?): List<PsiFile>? {
        if (element == null) {
            LOG.debug("ComponentWxmlUtils: resolveFromLiteral element=null")
            return null
        }
        val literal = when (element) {
            is JSLiteralExpression -> element
            else -> PsiTreeUtil.findChildOfType(element, JSLiteralExpression::class.java)
        }
        if (literal == null || !literal.isQuotedLiteral) {
            LOG.debug("ComponentWxmlUtils: resolveFromLiteral 未找到字符串字面量 elementType=${element.javaClass}")
            return null
        }
        val rawPath = literal.stringValue?.trim()?.takeIf { it.isNotEmpty() }
            ?: run {
                LOG.warn("ComponentWxmlUtils: 字面量无内容 literalText=${literal.text}")
                return null
            }
        val normalized = normalizeComponentPath(rawPath)
        LOG.debug("ComponentWxmlUtils: 尝试解析组件路径 raw=$rawPath normalized=$normalized hostFile=${literal.containingFile.name}")
        val project = literal.project
        val injectedManager = InjectedLanguageManager.getInstance(project)
        val topLevelFile = injectedManager.getTopLevelFile(literal)
        val hostVirtualFile = topLevelFile.virtualFile ?: literal.containingFile.originalFile.virtualFile
        if (hostVirtualFile == null) {
            LOG.warn("ComponentWxmlUtils: 无法获取宿主虚拟文件, componentPath=$rawPath literalFile=${literal.containingFile.name}")
            return null
        }

        var current = hostVirtualFile.parent
        while (current != null) {
            LOG.debug("ComponentWxmlUtils: 搜索基准目录 ${current.path}")
            val resolved = VfsUtil.findRelativeFile(normalized, current)
                ?: if (!normalized.endsWith(VUE_FILE_EXTENSION, true)) {
                    VfsUtil.findRelativeFile(normalized + VUE_FILE_EXTENSION, current)
                } else null
            if (resolved != null) {
                PsiManager.getInstance(project).findFile(resolved)?.let {
                    LOG.info("ComponentWxmlUtils: 解析到组件文件 ${resolved.path} (base=${current.path})")
                    return listOf(it)
                }
            }
            current = current.parent
        }
        LOG.warn("ComponentWxmlUtils: 未找到组件文件 componentPath=$rawPath host=${hostVirtualFile.path}")
        return null
    }

    private fun normalizeComponentPath(raw: String): String {
        var result = raw.replace('\\', '/').trim()
        while (result.startsWith("./")) {
            result = result.substring(2)
        }
        return result
    }
}
    private val LOG: Logger = Logger.getInstance(ComponentWxmlUtils::class.java)

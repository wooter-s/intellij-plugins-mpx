package org.jetbrains.mpxjs.lang.html.tag

import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CustomComponentDescriptorTest : BasePlatformTestCase() {

    fun testFallbackCollectsPropsFromVueFile() {
        myFixture.tempDirFixture.createFile(
            "pages/home/index.js",
            """
            module.exports = {
              usingComponents: {
                propaganda: "./propaganda.mpx"
              }
            }
            """.trimIndent(),
        )

        myFixture.tempDirFixture.createFile(
            "pages/home/index.mpx",
            """
            <template>
              <view>
                <propaganda custom-title="" count="" />
              </view>
            </template>
            """.trimIndent(),
        )

        myFixture.tempDirFixture.createFile(
            "pages/home/propaganda.mpx",
            """
            <template>
              <view />
            </template>
            <script>
            const baseProps = {
              count: {
                type: Number,
                value: 0
              }
            }

            export default {
              properties: {
                customTitle: {
                  type: String,
                  value: 'default'
                }
              },
              props: baseProps
            }
            </script>
            """.trimIndent(),
        )

        val indexXml = myFixture.configureFromTempProjectFile("pages/home/index.mpx") as XmlFile
        val componentTag = PsiTreeUtil.findChildrenOfType(indexXml, XmlTag::class.java)
            .first { it.name == "propaganda" }

        val jsVirtualFile = myFixture.tempDirFixture.getFile("pages/home/index.js")
        val jsFile = PsiManager.getInstance(project).findFile(jsVirtualFile!!) as JSFile
        val componentProperty = PsiTreeUtil.findChildrenOfType(jsFile, JSProperty::class.java)
            .first { it.name == "propaganda" }

        val descriptor = CustomComponentDescriptor(componentTag, componentProperty)
        val attributes = descriptor.getCustomComponentAttributeDescriptors(descriptor)

        assertTrue(attributes.isNotEmpty())
        assertSameElements(attributes.map { it.name }, listOf("customTitle", "count"))
    }
}


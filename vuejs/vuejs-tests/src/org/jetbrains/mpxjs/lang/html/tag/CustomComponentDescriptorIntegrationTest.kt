package org.jetbrains.mpxjs.lang.html.tag

import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.psi.util.PsiTreeUtil

class CustomComponentDescriptorIntegrationTest : BasePlatformTestCase() {

    fun testMpxComponentPropertiesResolvedFromVueFile() {
        myFixture.tempDirFixture.createFile(
            "home/components/home-card/propaganda.mpx",
            """
            <template>
              <view />
            </template>

            <script lang=\"ts\">
            import { createComponent } from '@mpxjs/core'

            createComponent({
              properties: {
                progaganda: {
                  type: Object,
                  value: {}
                }
              }
            })
            </script>

            <style lang=\"stylus\"></style>

            <script name='json'>
            module.exports = {
              component: true,
              usingComponents: {}
            }
            </script>
            """.trimIndent(),
        )

        myFixture.tempDirFixture.createFile(
            "home/components/home-card/index.mpx",
            """
            <template>
              <propaganda progaganda=\"{{ data }}\" />
            </template>

            <script lang=\"ts\">
            import { createComponent } from '@mpxjs/core'

            createComponent({
              setup () {
                return {
                  data: {}
                }
              }
            })
            </script>

            <style lang=\"stylus\"></style>

            <script name='json'>
            module.exports = {
              component: true,
              usingComponents: {
                propaganda: './propaganda.mpx'
              }
            }
            </script>
            """.trimIndent(),
        )

        val xml = myFixture.configureFromTempProjectFile("home/components/home-card/index.mpx") as XmlFile
        val tag = PsiTreeUtil.findChildrenOfType(xml, XmlTag::class.java).first { it.name == "propaganda" }

        val descriptor = tag.descriptor as? CustomComponentDescriptor
        assertNotNull("自定义组件描述符未解析", descriptor)

        val attributeDescriptor = descriptor!!.getAttributeDescriptor("progaganda", tag)
        assertNotNull("未解析到属性 progaganda", attributeDescriptor)
    }
}


package org.jetbrains.mpxjs.lang.html.tag

import com.intellij.lang.javascript.psi.JSFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ComponentJsUtilsTest : BasePlatformTestCase() {

    fun testFindPropertiesFromPropsOption() {
        val jsFile = myFixture.configureByText(
            "sampleProps.js",
            """
            Component({
              props: {
                foo: String,
                bar: {
                  type: Number
                }
              }
            })
            """.trimIndent(),
        ) as JSFile

        val properties = ComponentJsUtils.findPropertiesItems(jsFile)

        assertNotNull(properties)
        assertSameElements(properties!!.mapNotNull { it.name }, listOf("foo", "bar"))
    }

    fun testFindPropertiesFromMiniProgramPropertiesOption() {
        val jsFile = myFixture.configureByText(
            "sampleProperties.js",
            """
            Component({
              properties: {
                customTitle: {
                  type: String,
                  value: 'default'
                }
              }
            })
            """.trimIndent(),
        ) as JSFile

        val properties = ComponentJsUtils.findPropertiesItems(jsFile)

        assertNotNull(properties)
        assertSameElements(properties!!.mapNotNull { it.name }, listOf("customTitle"))
    }
}

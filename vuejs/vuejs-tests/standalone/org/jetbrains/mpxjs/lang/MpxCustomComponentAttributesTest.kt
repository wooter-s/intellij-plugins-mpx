// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.lang

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MpxCustomComponentAttributesTest : BasePlatformTestCase() {

  fun testExternalScriptPropertyCompletionAndNavigation() {
    configureComponent()
    configurePage("showPri<caret>")

    myFixture.completeBasic()
    assertCompletionResult("showPriceRule")

    configurePage("showPrice<caret>Rule=\"{{showPriceRule}}\"")
    assertReferenceResolves(
      "showPrice<caret>Rule",
      "index.ts",
      "showPriceRule",
    )
  }

  fun testStaticTriggerEventCompletionUsesAllWechatEventPrefixes() {
    configureComponent()

    for ((typedAttribute, expectedAttribute) in listOf(
      "bind:select<caret>" to "bind:selectVehicle",
      "bindselect<caret>" to "bindselectVehicle",
      "catch:select<caret>" to "catch:selectVehicle",
      "catchselect<caret>" to "catchselectVehicle",
      "capture-bind:select<caret>" to "capture-bind:selectVehicle",
      "capture-bindselect<caret>" to "capture-bindselectVehicle",
      "capture-catch:select<caret>" to "capture-catch:selectVehicle",
      "capture-catchselect<caret>" to "capture-catchselectVehicle",
      "mut-bind:select<caret>" to "mut-bind:selectVehicle",
      "mut-bindselect<caret>" to "mut-bindselectVehicle",
    )) {
      configurePage(typedAttribute)
      myFixture.completeBasic()
      assertCompletionResult(expectedAttribute, "selectVehicle")
    }
  }

  fun testEventAttributesNavigateToExternalTriggerEventCalls() {
    configureComponent()
    configurePage(
      """
        bind:selectVehicle="handleSelect"
        bindresetLocation="handleReset"
        catch:selectVehicle="handleSelect"
        catchresetLocation="handleReset"
        capture-bind:selectVehicle="handleSelect"
        capture-bindresetLocation="handleReset"
        capture-catch:selectVehicle="handleSelect"
        capture-catchresetLocation="handleReset"
        mut-bind:selectVehicle="handleSelect"
        mut-bindresetLocation="handleReset"
      """.trimIndent(),
    )

    for ((from, target) in listOf(
      "bind:select<caret>Vehicle" to "selectVehicle",
      "bindreset<caret>Location" to "resetLocation",
      "catch:select<caret>Vehicle" to "selectVehicle",
      "catchreset<caret>Location" to "resetLocation",
      "capture-bind:select<caret>Vehicle" to "selectVehicle",
      "capture-bindreset<caret>Location" to "resetLocation",
      "capture-catch:select<caret>Vehicle" to "selectVehicle",
      "capture-catchreset<caret>Location" to "resetLocation",
      "mut-bind:select<caret>Vehicle" to "selectVehicle",
      "mut-bindreset<caret>Location" to "resetLocation",
    )) {
      assertReferenceResolves(from, "index.ts", target)
    }
  }

  fun testDynamicTriggerEventNameIsNotSuggested() {
    configureComponent()
    configurePage("bind:<caret>")

    myFixture.completeBasic()
    val completions = myFixture.lookupElementStrings.orEmpty()
    assertContainsElements(completions, "selectVehicle", "resetLocation", "ctxEvent")
    assertDoesntContain(completions, "dynamicEventName", "bind:dynamicEventName")
  }

  private fun configureComponent() {
    myFixture.addFileToProject(
      "components/vehicle-card/index.ts",
      """
        import { createComponent } from '@mpxjs/core'

        createComponent({
          properties: {
            showPriceRule: Boolean,
          },
          setup(props, context) {
            context.triggerEvent('selectVehicle')
            const { triggerEvent } = context
            triggerEvent('resetLocation')
            const ctx = context
            ctx.triggerEvent('ctxEvent')
            context.triggerEvent(dynamicEventName)
          },
        })
      """.trimIndent(),
    )
    myFixture.addFileToProject(
      "components/vehicle-card/index.mpx",
      """
        <template><view /></template>
        <script lang="ts" src="./index.ts"></script>
      """.trimIndent(),
    )
  }

  private fun configurePage(attributes: String) {
    myFixture.configureByText(
      "page.mpx",
      """
        <template>
          <vehicle-reservation-card
            $attributes
          />
        </template>
        <script name="json">
        const usingComponents = {
          "vehicle-reservation-card": "./components/vehicle-card/index.mpx",
        }
        module.exports = { usingComponents }
        </script>
      """.trimIndent(),
    )
  }

  private fun assertReferenceResolves(
    signature: String,
    expectedFileName: String,
    expectedTargetText: String,
  ) {
    val markerOffset = signature.indexOf(CARET_MARKER)
    val plainSignature = signature.replace(CARET_MARKER, "")
    val signatureOffset = myFixture.file.text.indexOf(plainSignature)
    assertTrue("Cannot find `$plainSignature` in ${myFixture.file.name}", signatureOffset >= 0)

    myFixture.editor.caretModel.moveToOffset(signatureOffset + markerOffset)
    val targets = GotoDeclarationAction.findAllTargetElements(
      project,
      myFixture.editor,
      myFixture.editor.caretModel.offset,
    ).toList().ifEmpty {
      val reference = myFixture.getReferenceAtCaretPosition()
      when (reference) {
        is PsiPolyVariantReference -> reference.multiResolve(false).mapNotNull { it.element }
        else -> listOfNotNull(reference?.resolve())
      }
    }
    assertTrue(
      "Expected `$signature` to resolve into $expectedFileName near `$expectedTargetText`, got ${
        targets.map { "${it.containingFile?.name}:${it.text}" }
      }",
      targets.any {
        it.containingFile?.name == expectedFileName && it.orParentContains(expectedTargetText)
      },
    )
  }

  private fun PsiElement.orParentContains(expectedText: String): Boolean =
    generateSequence(this) { it.parent }
      .take(4)
      .any { expectedText in it.text }

  private fun assertCompletionResult(expectedAttribute: String, alternativeLookup: String? = null) {
    val lookupStrings = myFixture.lookupElementStrings.orEmpty()
    assertTrue(
      "Expected completion `$expectedAttribute`, lookup=$lookupStrings, text=${myFixture.file.text}",
      expectedAttribute in lookupStrings
      || (alternativeLookup != null && alternativeLookup in lookupStrings)
      || expectedAttribute in myFixture.file.text,
    )
  }

  companion object {
    private const val CARET_MARKER = "<caret>"
  }
}

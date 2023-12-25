package org.jetbrains.mpxjs.intentions

import com.intellij.codeInsight.intention.FileModifier.SafeFieldForPreview
import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.lang.javascript.intentions.JavaScriptIntention
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlElementType
import org.jetbrains.mpxjs.VueBundle
import org.jetbrains.mpxjs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.mpxjs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveInfo
import org.jetbrains.mpxjs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveKind
import org.jetbrains.mpxjs.context.isVueContext
import org.jetbrains.mpxjs.model.VueModelDirectiveProperties
import org.jetbrains.mpxjs.web.getModel

//`VueExpandVModelIntention` 是一个 Kotlin 类，它继承自 `JavaScriptIntention`。这个类是用于实现一个特定的代码意图（Intention）的，这个意图是用于扩展 Vue 中的 `v-model` 指令。
//
//在 Vue 中，`v-model` 指令是一个双向数据绑定的语法糖，它可以在内部自动更新数据和视图。但是，在某些情况下，你可能需要更细粒度的控制，例如修改更新数据的事件或处理数据的方式。这时，你就可以使用 `VueExpandVModelIntention` 来扩展 `v-model` 指令。
//
//`VueExpandVModelIntention` 类的主要方法是 `invoke`，它会被调用当用户选择应用这个意图时。在 `invoke` 方法中，它首先获取 `v-model` 指令的相关信息，然后根据这些信息修改 `v-model` 指令，使其变为一个更详细的数据绑定表达式。
//
//例如，如果你有一个 `v-model` 指令如下：
//
//```html
//<input v-model="message">
//```
//
//应用 `VueExpandVModelIntention` 后，它可能会变为：
//
//```html
//<input :value="message" @input="message = $event.target.value">
//```
//
//这样，你就可以更灵活地控制数据的更新和处理。
class VueExpandVModelIntention : JavaScriptIntention() {
  override fun getFamilyName(): String = VueBundle.message("vue.template.intention.v-model.expand.family.name")
  override fun getText(): String = this.familyName

  @SafeFieldForPreview
  private val validModifiers = setOf("lazy", "number", "trim")

  override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
    element.node.elementType == XmlElementType.XML_NAME
    && element.parent
      ?.let {
        it.node.elementType == XmlElementType.XML_ATTRIBUTE
        && it.isValid && it is XmlAttribute
        && it.parent?.descriptor is WebSymbolElementDescriptor
        && isValidVModel(it)
      } == true
    && isVueContext(element)

  private fun isValidVModel(attribute: XmlAttribute): Boolean {
    val info = VueAttributeNameParser.parse((attribute.name), attribute.parent)
    return (info as? VueDirectiveInfo)?.directiveKind == VueDirectiveKind.MODEL
           && validModifiers.containsAll(info.modifiers)
  }

  override fun invoke(project: Project, editor: Editor?, psiElement: PsiElement) {
    editor ?: return
    val parent: PsiElement = psiElement.parent
    val modelAttribute = parent as XmlAttribute
    val componentTag = modelAttribute.parent
    val componentDescriptor = componentTag.descriptor as? WebSymbolElementDescriptor ?: return

    val model = componentDescriptor.getModel()
    val defaultModel = VueModelDirectiveProperties.getDefault(psiElement)
    var event = model.event ?: defaultModel.event
    val prop = model.prop ?: defaultModel.prop
    val info = VueAttributeNameParser.parse(parent.name, parent.parent)

    val modifiers = info.modifiers
    var eventValue = "\$event"
    if (modifiers.contains("trim")) {
      eventValue = "typeof $eventValue === 'string' ? $eventValue.trim() : $eventValue"
    }
    if (modifiers.contains("number")) {
      eventValue = "isNaN(parseFloat($eventValue)) ? $eventValue : parseFloat($eventValue)"
    }
    if (modifiers.contains("lazy")) {
      event = "change"
    }
    modelAttribute.name = ":$prop"
    componentTag.setAttribute("@$event", "${modelAttribute.value} = $eventValue")
  }
}

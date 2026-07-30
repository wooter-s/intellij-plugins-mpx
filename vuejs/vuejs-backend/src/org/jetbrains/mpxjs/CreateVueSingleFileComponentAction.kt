// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys.PROJECT
import com.intellij.openapi.actionSystem.PlatformDataKeys.PSI_ELEMENT
import com.intellij.openapi.actionSystem.PlatformDataKeys.PSI_FILE
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.jetbrains.annotations.Nls
import org.jetbrains.mpxjs.VueCreateFromTemplateHandler.Companion.MPX_RECENT_TEMPLATES
import org.jetbrains.mpxjs.context.getVueClassComponentLibrary
import org.jetbrains.mpxjs.context.hasVueFiles
import org.jetbrains.mpxjs.context.isVueContext
import org.jetbrains.mpxjs.context.supportsScriptSetup

class CreateVueSingleFileComponentAction : CreateFileFromTemplateAction(), DumbAware {
  companion object {

    @Nls
    private val name = VueBundle.message("action.CreateVueSingleFileComp.text")
  }

  override fun isAvailable(dataContext: DataContext): Boolean =
    super.isAvailable(dataContext)
    && (PROJECT.getData(dataContext)?.let { hasVueFiles(it) } == true
        || (PSI_ELEMENT.getData(dataContext) ?: PSI_FILE.getData(dataContext))?.let { isVueContext(it) } == true)

  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder
      .setTitle(VueBundle.message("vue.create.single.file.component.action.dialog.title", name))

    val recentNames = PropertiesComponent.getInstance(project).getList(MPX_RECENT_TEMPLATES) ?: emptyList()

    listOfNotNull(
      VueCreateFromTemplateHandler.MPX_CLASS_API_TEMPLATE_NAME.takeIf { getVueClassComponentLibrary(directory) != null },
      VueCreateFromTemplateHandler.MPX_COMPOSITION_API_TEMPLATE_NAME.takeIf { supportsScriptSetup(directory) },
      VueCreateFromTemplateHandler.MPX_OPTIONS_API_TEMPLATE_NAME
    )
      .sortedByDescending { recentNames.indexOf(it) }
      .forEach { name ->
        builder.addKind(
          when (name) {
            VueCreateFromTemplateHandler.MPX_CLASS_API_TEMPLATE_NAME -> VueBundle.message(
              "vue.create.single.file.component.template.class.api")
            VueCreateFromTemplateHandler.MPX_COMPOSITION_API_TEMPLATE_NAME -> VueBundle.message(
              "vue.create.single.file.component.template.composition.api")
            VueCreateFromTemplateHandler.MPX_OPTIONS_API_TEMPLATE_NAME -> VueBundle.message(
              "vue.create.single.file.component.template.options.api")
            else -> throw IllegalStateException(name)
          },
          VuejsIcons.Mpx,
          name
        )
      }
  }

  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
    VueBundle.message("vue.create.single.file.component.action.name", newName)

}

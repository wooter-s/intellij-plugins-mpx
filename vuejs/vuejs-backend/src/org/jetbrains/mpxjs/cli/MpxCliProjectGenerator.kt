// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.cli

import com.intellij.execution.filters.Filter
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.javascript.CreateRunConfigurationUtil
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.packages.NodePackageUtil
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.lang.javascript.boilerplate.NpxPackageDescriptor
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.PathUtil
import org.jetbrains.mpxjs.VueBundle
import org.jetbrains.mpxjs.VuejsIcons
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.swing.Icon
import javax.swing.JPanel

class MpxCliProjectGenerator : NpmPackageProjectGenerator() {
  private val LOG = Logger.getInstance(MpxCliProjectGenerator::class.java)

  private val DEFAULT_PROJECT_SETUP_INITIALLY_SELECTED = true
  private val DEFAULT_PROJECT_SETUP_KEY = Key.create<Boolean>("mpx.project.generator.default.setup")

  private val MPX_CLI_PACKAGE_NAME = "@mpxjs/cli"
  private val MPX_EXECUTABLE = "mpx"
  private val CREATE_COMMAND = "create"

  override fun getId(): String {
    return "Mpx"
  }

  override fun getName(): String {
    return VueBundle.message("vue.project.generator.name")
  }

  override fun getDescription(): String {
    return VueBundle.message("vue.project.generator.description")
  }

  override fun getIcon(): Icon {
    return VuejsIcons.Mpx
  }

  override fun createPeer(): ProjectGeneratorPeer<Settings> {
    val defaultSetupCheckbox = JBCheckBox(VueBundle.message("vue.project.generator.use.default.project.setup"),
                                          DEFAULT_PROJECT_SETUP_INITIALLY_SELECTED)
    return object : NpmPackageGeneratorPeer() {
      override fun createPanel(): JPanel {
        val panel = super.createPanel()
        panel.add(defaultSetupCheckbox)
        return panel
      }

      override fun addExtraFields(settingsStep: SettingsStep) {
        settingsStep.addSettingsComponent(defaultSetupCheckbox)
      }

      override fun getSettings(): Settings {
        val settings = super.getSettings()
        settings.putUserData(DEFAULT_PROJECT_SETUP_KEY, defaultSetupCheckbox.isSelected)
        return settings
      }
    }
  }

  override fun customizeModule(baseDir: VirtualFile, entry: ContentEntry?) {}

  override fun generatorArgs(project: Project, dir: VirtualFile, settings: Settings): Array<String> {
    val default = settings.getUserData(DEFAULT_PROJECT_SETUP_KEY) ?: DEFAULT_PROJECT_SETUP_INITIALLY_SELECTED
    val workingDir = if (generateInTemp()) dir.name else "."

    return if (default) arrayOf(CREATE_COMMAND, "--default", workingDir) else arrayOf(CREATE_COMMAND, workingDir)
  }

  override fun generatorArgs(project: Project, baseDir: VirtualFile): Array<String> {
    return emptyArray()
  }

  override fun filters(project: Project, baseDir: VirtualFile): Array<Filter> {
    return emptyArray()
  }

  override fun executable(pkg: NodePackage): String {
    return pkg.systemDependentPath + File.separator + "bin" + File.separator + "mpx.js"
  }

  override fun packageName(): String {
    return MPX_CLI_PACKAGE_NAME
  }

  override fun packageNames(): List<String> {
    return listOf(MPX_CLI_PACKAGE_NAME)
  }

  override fun presentablePackageName(): String {
    return VueBundle.message("vue.project.generator.presentable.package.name")
  }

  override fun getNpxCommands(): List<NpxPackageDescriptor.NpxCommand> {
    return listOf(
      NpxPackageDescriptor.NpxCommand(MPX_CLI_PACKAGE_NAME, MPX_EXECUTABLE),
    )
  }

  override fun validateProjectPath(path: String): String? {
    val error = NodePackageUtil.validateNpmPackageName(PathUtil.getFileName(path))
    return error ?: super.validateProjectPath(path)
  }

  override fun onProcessHandlerCreated(processHandler: ProcessHandler) {
    processHandler.addProcessListener(object : ProcessAdapter() {
      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        // Handle prompts from @mpxjs/cli
        if (event.text.contains("Generate project in current directory?")) {
          event.processHandler.removeProcessListener(this)
          val processInput = event.processHandler.processInput
          if (processInput != null) {
            try {
              processInput.write("yes\n".toByteArray(StandardCharsets.UTF_8))
              processInput.flush()
            }
            catch (e: IOException) {
              LOG.warn("Failed to write 'yes' to the Mpx CLI console.", e)
            }
          }
        }
      }
    })
  }

  override fun onGettingSmartAfterProjectGeneration(project: Project, baseDir: VirtualFile) {
    super.onGettingSmartAfterProjectGeneration(project, baseDir)
    // Default Mpx project uses serve command
    CreateRunConfigurationUtil.debugConfiguration(project, 8080)
    CreateRunConfigurationUtil.npmConfiguration(project, "serve")
  }

  override fun generateInTemp(): Boolean = true
}

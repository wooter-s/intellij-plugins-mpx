package com.jetbrains.plugins.meteor.projectGenerator;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MeteorTemplatesFactory extends ProjectTemplatesFactory {
  @Override
  public String @NotNull [] getGroups() {
    return new String[]{WebModuleBuilder.GROUP_NAME};
  }

  @Override
  public ProjectTemplate @NotNull [] createTemplates(@Nullable String group, @NotNull WizardContext context) {
    return new ProjectTemplate[]{new MeteorProjectTemplateGenerator()};
  }
}

// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript;

import com.intellij.ide.fileTemplates.DefaultTemplatePropertiesProvider;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public final class ActionScriptAccessModifierTemplatePropertyProvider implements DefaultTemplatePropertiesProvider {

  @Override
  public void fillProperties(@NotNull PsiDirectory directory, @NotNull Properties props) {
    props.setProperty(ActionScriptCreateClassOrInterfaceFix.ACCESS_MODIFIER_PROPERTY, "public");
  }
}

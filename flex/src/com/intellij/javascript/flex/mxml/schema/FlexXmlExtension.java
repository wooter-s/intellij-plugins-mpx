package com.intellij.javascript.flex.mxml.schema;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.TagNameReference;
import com.intellij.xml.DefaultXmlExtension;

public final class FlexXmlExtension extends DefaultXmlExtension {
  @Override
  public boolean isAvailable(final PsiFile file) {
    return JavaScriptSupportLoader.isFlexMxmFile(file);
  }

  @Override
  public TagNameReference createTagNameReference(final ASTNode nameElement, final boolean startTagFlag) {
    return new MxmlTagNameReference(nameElement, startTagFlag);
  }
}

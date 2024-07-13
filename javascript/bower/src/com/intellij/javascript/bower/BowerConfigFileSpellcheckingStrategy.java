package com.intellij.javascript.bower;

import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;

public class BowerConfigFileSpellcheckingStrategy extends SpellcheckingStrategy {
  @NotNull
  @Override
  public Tokenizer getTokenizer(PsiElement element) {
    return SpellcheckingStrategy.EMPTY_TOKENIZER;
  }

  @Override
  public boolean isMyContext(@NotNull PsiElement element) {
    return BowerPackageUtil.getContainingBowerJsonFile(element) != null;
  }
}

// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.searchers;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.plugins.drools.DroolsFileType;
import com.intellij.plugins.drools.lang.psi.impl.DroolsFakePsiMethod;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

public final class DroolsFunctionMethodUsageSearcher extends QueryExecutorBase<PsiReference, MethodReferencesSearch.SearchParameters> {

  public DroolsFunctionMethodUsageSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull MethodReferencesSearch.SearchParameters queryParameters, @NotNull Processor<? super PsiReference> consumer) {
    final PsiMethod method = queryParameters.getMethod();
    if (method instanceof DroolsFakePsiMethod) {
      SearchScope scope = queryParameters.getEffectiveSearchScope();
      if (scope instanceof GlobalSearchScope) {
        scope = GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope)scope, DroolsFileType.DROOLS_FILE_TYPE);
      }
      queryParameters.getOptimizer().searchWord(method.getName(), scope, UsageSearchContext.ANY, true, method);
    }
  }
}

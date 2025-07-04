// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.libraries.vuex.codeInsight.refs

import org.jetbrains.mpxjs.libraries.vuex.model.store.VuexContainer
import org.jetbrains.mpxjs.libraries.vuex.model.store.VuexNamedSymbol

typealias VuexSymbolAccessor = (VuexContainer) -> Map<String, VuexNamedSymbol>

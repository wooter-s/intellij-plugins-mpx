// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.libraries.vuex.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.psi.stubs.StubIndexKey
import org.jetbrains.mpxjs.index.VueIndexBase

class VuexStoreIndex : VueIndexBase<JSImplicitElementProvider>(VUEX_STORE_INDEX_KEY)

val VUEX_STORE_INDEX_KEY: StubIndexKey<String, JSImplicitElementProvider> = StubIndexKey.createIndexKey("mpx.vuex.store.index")
val VUEX_STORE_INDEX_JS_KEY: String = VueIndexBase.createJSKey(VUEX_STORE_INDEX_KEY)
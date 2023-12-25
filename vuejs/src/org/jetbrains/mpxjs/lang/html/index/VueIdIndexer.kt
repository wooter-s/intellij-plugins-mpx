// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.html.index

import com.intellij.psi.impl.cache.impl.BaseFilterLexerUtil
import com.intellij.psi.impl.cache.impl.id.IdIndexEntry
import com.intellij.psi.impl.cache.impl.id.LexingIdIndexer
import com.intellij.util.indexing.FileContent

//总的来说，VueIdIndexer类的主要作用是为Vue.js项目中的文件内容创建索引，以便于后续的代码分析和处理。
class VueIdIndexer : LexingIdIndexer {
  override fun map(inputData: FileContent): Map<IdIndexEntry, Int> =
    BaseFilterLexerUtil.calcIdEntries(inputData) { consumer ->
      VueFilterLexer(consumer, inputData.project, inputData.file)
    }

  override fun getVersion(): Int = 1
}

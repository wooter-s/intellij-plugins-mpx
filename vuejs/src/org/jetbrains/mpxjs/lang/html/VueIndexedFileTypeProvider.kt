// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.html

import com.intellij.lang.javascript.index.IndexedFileTypeProvider
import com.intellij.openapi.fileTypes.FileType

//IndexedFileTypeProvider 是 IntelliJ IDEA 中的一个接口，它用于定义哪些文件类型应该被索引。索引是一种数据结构，它可以加快搜索和查找速度。在 IntelliJ IDEA 中，索引用于快速查找文件、类、方法等。
class VueIndexedFileTypeProvider : IndexedFileTypeProvider {
  override fun getFileTypesToIndex(): Array<FileType> = arrayOf(VueFileType.INSTANCE)
}

// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.libraries.nuxt.library

import com.intellij.platform.workspace.storage.*
import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceList
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

interface NuxtFolderEntity: WorkspaceEntity {

  val nuxtFolderUrl: VirtualFileUrl
  val libraryFileUrls: List<VirtualFileUrl>

  object MyEntitySource : EntitySource

  //region generated code
  @GeneratedCodeApiVersion(2)
  interface Builder : NuxtFolderEntity, WorkspaceEntity.Builder<NuxtFolderEntity> {
    override var entitySource: EntitySource
    override var nuxtFolderUrl: VirtualFileUrl
    override var libraryFileUrls: MutableList<VirtualFileUrl>
  }

  companion object : EntityType<NuxtFolderEntity, Builder>() {
    @JvmOverloads
    @JvmStatic
    @JvmName("create")
    operator fun invoke(nuxtFolderUrl: VirtualFileUrl,
                        libraryFileUrls: List<VirtualFileUrl>,
                        entitySource: EntitySource,
                        init: (Builder.() -> Unit)? = null): NuxtFolderEntity {
      val builder = builder()
      builder.nuxtFolderUrl = nuxtFolderUrl
      builder.libraryFileUrls = libraryFileUrls.toMutableWorkspaceList()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion
}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: NuxtFolderEntity,
                                      modification: NuxtFolderEntity.Builder.() -> Unit): NuxtFolderEntity = modifyEntity(
  NuxtFolderEntity.Builder::class.java, entity, modification)
//endregion

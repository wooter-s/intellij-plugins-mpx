// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.libraries.nuxt.library

import com.intellij.javascript.nodejs.library.yarn.pnp.workspaceModel.createStorageFrom
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager

class NuxtFolderLibraryStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    NuxtFolderModelSynchronizer(project).sync()
  }
}

class NuxtFolderModelSynchronizer(private val project: Project) {

  private val workspaceModel: WorkspaceModel = WorkspaceModel.getInstance(project)
  private val nuxtFolderManager: NuxtFolderManager = NuxtFolderManager.getInstance(project)
  private val libraries: List<NuxtFolderLibrary> = nuxtFolderManager.nuxtFolders.map {
    NuxtFolderLibrary(it)
  }
  private val virtualFileUrlManager: VirtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()

  fun sync() {
    val actualEntities = buildActualEntities()
    if (areEntitiesOutdated(actualEntities)) {
      updateEntities(actualEntities)
    }
  }

  private fun buildActualEntities(): List<NuxtFolderEntity.Builder> {
    return libraries.map {
      NuxtFolderManager.createEntity(it, virtualFileUrlManager)
    }
  }

  private fun areEntitiesOutdated(actualEntities: List<NuxtFolderEntity.Builder>): Boolean {
    val workspaceModelEntities: List<NuxtFolderEntity> = getWorkspaceModelEntities()
    if (workspaceModelEntities.size != actualEntities.size) return true
    val wrappedWorkspaceModelEntities = workspaceModelEntities.mapTo(HashSet()) { NuxtFolderEntityWrapper(it) }
    val wrappedActualEntities = actualEntities.mapTo(HashSet()) { NuxtFolderEntityWrapper(it) }
    return wrappedWorkspaceModelEntities != wrappedActualEntities
  }

  private fun getWorkspaceModelEntities(): List<NuxtFolderEntity> {
    return workspaceModel.currentSnapshot.entities(NuxtFolderEntity::class.java).toList()
  }

  private fun updateEntities(actualEntities: List<NuxtFolderEntity.Builder>) {
    val entitiesStorage = createStorageFrom(actualEntities)
    NuxtFolderManager.invokeUnderWriteAction(project) {
      workspaceModel.updateProjectModel(".nuxt outdated (new count: ${actualEntities.size})") { storage ->
        storage.replaceBySource({ it === NuxtFolderEntity.MyEntitySource }, entitiesStorage)
      }
    }
  }
}

private class NuxtFolderEntityWrapper(
  entity: Any
) {
  private val nuxtFolderUrl: VirtualFileUrl = (entity as? NuxtFolderEntity)?.nuxtFolderUrl
                                              ?: (entity as NuxtFolderEntity.Builder).nuxtFolderUrl
  private val libraryFileUrls: Set<VirtualFileUrl> = (entity as? NuxtFolderEntity)?.libraryFileUrls?.toHashSet()
                                                     ?: (entity as NuxtFolderEntity.Builder).libraryFileUrls.toHashSet()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as NuxtFolderEntityWrapper

    return nuxtFolderUrl == other.nuxtFolderUrl && libraryFileUrls == other.libraryFileUrls
  }

  override fun hashCode(): Int {
    var result = nuxtFolderUrl.hashCode()
    result = 31 * result + libraryFileUrls.hashCode()
    return result
  }
}

// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.libraries.nuxt.library

import com.intellij.platform.workspace.storage.EntityInformation
import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityStorage
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.GeneratedCodeImplVersion
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.impl.ConnectionId
import com.intellij.platform.workspace.storage.impl.ModifiableWorkspaceEntityBase
import com.intellij.platform.workspace.storage.impl.WorkspaceEntityBase
import com.intellij.platform.workspace.storage.impl.WorkspaceEntityData
import com.intellij.platform.workspace.storage.impl.containers.MutableWorkspaceList
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceList
import com.intellij.platform.workspace.storage.metadata.model.EntityMetadata
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

@GeneratedCodeApiVersion(2)
@GeneratedCodeImplVersion(3)
open class NuxtFolderEntityImpl(private val dataSource: NuxtFolderEntityData) : NuxtFolderEntity, WorkspaceEntityBase(dataSource) {

  private companion object {


    private val connections = listOf<ConnectionId>(
    )

  }

  override val nuxtFolderUrl: VirtualFileUrl
    get() = dataSource.nuxtFolderUrl

  override val libraryFileUrls: List<VirtualFileUrl>
    get() = dataSource.libraryFileUrls

  override val entitySource: EntitySource
    get() = dataSource.entitySource

  override fun connectionIdList(): List<ConnectionId> {
    return connections
  }


  class Builder(result: NuxtFolderEntityData?) : ModifiableWorkspaceEntityBase<NuxtFolderEntity, NuxtFolderEntityData>(
    result), NuxtFolderEntity.Builder {
    constructor() : this(NuxtFolderEntityData())

    override fun applyToBuilder(builder: MutableEntityStorage) {
      if (this.diff != null) {
        if (existsInBuilder(builder)) {
          this.diff = builder
          return
        }
        else {
          error("Entity NuxtFolderEntity is already created in a different builder")
        }
      }

      this.diff = builder
      this.snapshot = builder
      addToBuilder()
      this.id = getEntityData().createEntityId()
      // After adding entity data to the builder, we need to unbind it and move the control over entity data to builder
      // Builder may switch to snapshot at any moment and lock entity data to modification
      this.currentEntityData = null

      index(this, "nuxtFolderUrl", this.nuxtFolderUrl)
      index(this, "libraryFileUrls", this.libraryFileUrls)
      // Process linked entities that are connected without a builder
      processLinkedEntities(builder)
      checkInitialization() // TODO uncomment and check failed tests
    }

    private fun checkInitialization() {
      val _diff = diff
      if (!getEntityData().isEntitySourceInitialized()) {
        error("Field WorkspaceEntity#entitySource should be initialized")
      }
      if (!getEntityData().isNuxtFolderUrlInitialized()) {
        error("Field NuxtFolderEntity#nuxtFolderUrl should be initialized")
      }
      if (!getEntityData().isLibraryFileUrlsInitialized()) {
        error("Field NuxtFolderEntity#libraryFileUrls should be initialized")
      }
    }

    override fun connectionIdList(): List<ConnectionId> {
      return connections
    }

    override fun afterModification() {
      val collection_libraryFileUrls = getEntityData().libraryFileUrls
      if (collection_libraryFileUrls is MutableWorkspaceList<*>) {
        collection_libraryFileUrls.cleanModificationUpdateAction()
      }
    }

    // Relabeling code, move information from dataSource to this builder
    override fun relabel(dataSource: WorkspaceEntity, parents: Set<WorkspaceEntity>?) {
      dataSource as NuxtFolderEntity
      if (this.entitySource != dataSource.entitySource) this.entitySource = dataSource.entitySource
      if (this.nuxtFolderUrl != dataSource.nuxtFolderUrl) this.nuxtFolderUrl = dataSource.nuxtFolderUrl
      if (this.libraryFileUrls != dataSource.libraryFileUrls) this.libraryFileUrls = dataSource.libraryFileUrls.toMutableList()
      updateChildToParentReferences(parents)
    }


    override var entitySource: EntitySource
      get() = getEntityData().entitySource
      set(value) {
        checkModificationAllowed()
        getEntityData(true).entitySource = value
        changedProperty.add("entitySource")

      }

    override var nuxtFolderUrl: VirtualFileUrl
      get() = getEntityData().nuxtFolderUrl
      set(value) {
        checkModificationAllowed()
        getEntityData(true).nuxtFolderUrl = value
        changedProperty.add("nuxtFolderUrl")
        val _diff = diff
        if (_diff != null) index(this, "nuxtFolderUrl", value)
      }

    private val libraryFileUrlsUpdater: (value: List<VirtualFileUrl>) -> Unit = { value ->
      val _diff = diff
      if (_diff != null) index(this, "libraryFileUrls", value)
      changedProperty.add("libraryFileUrls")
    }
    override var libraryFileUrls: MutableList<VirtualFileUrl>
      get() {
        val collection_libraryFileUrls = getEntityData().libraryFileUrls
        if (collection_libraryFileUrls !is MutableWorkspaceList) return collection_libraryFileUrls
        if (diff == null || modifiable.get()) {
          collection_libraryFileUrls.setModificationUpdateAction(libraryFileUrlsUpdater)
        }
        else {
          collection_libraryFileUrls.cleanModificationUpdateAction()
        }
        return collection_libraryFileUrls
      }
      set(value) {
        checkModificationAllowed()
        getEntityData(true).libraryFileUrls = value
        libraryFileUrlsUpdater.invoke(value)
      }

    override fun getEntityClass(): Class<NuxtFolderEntity> = NuxtFolderEntity::class.java
  }
}

class NuxtFolderEntityData : WorkspaceEntityData<NuxtFolderEntity>() {
  lateinit var nuxtFolderUrl: VirtualFileUrl
  lateinit var libraryFileUrls: MutableList<VirtualFileUrl>

  internal fun isNuxtFolderUrlInitialized(): Boolean = ::nuxtFolderUrl.isInitialized
  internal fun isLibraryFileUrlsInitialized(): Boolean = ::libraryFileUrls.isInitialized

  override fun wrapAsModifiable(diff: MutableEntityStorage): WorkspaceEntity.Builder<NuxtFolderEntity> {
    val modifiable = NuxtFolderEntityImpl.Builder(null)
    modifiable.diff = diff
    modifiable.snapshot = diff
    modifiable.id = createEntityId()
    return modifiable
  }

  override fun createEntity(snapshot: EntityStorage): NuxtFolderEntity {
    return getCached(snapshot) {
      val entity = NuxtFolderEntityImpl(this)
      entity.snapshot = snapshot
      entity.id = createEntityId()
      entity
    }
  }

  override fun getMetadata(): EntityMetadata {
    return MetadataStorageImpl.getMetadataByTypeFqn("org.jetbrains.mpxjs.libraries.nuxt.library.NuxtFolderEntity") as EntityMetadata
  }

  override fun clone(): NuxtFolderEntityData {
    val clonedEntity = super.clone()
    clonedEntity as NuxtFolderEntityData
    clonedEntity.libraryFileUrls = clonedEntity.libraryFileUrls.toMutableWorkspaceList()
    return clonedEntity
  }

  override fun getEntityInterface(): Class<out WorkspaceEntity> {
    return NuxtFolderEntity::class.java
  }

  override fun serialize(ser: EntityInformation.Serializer) {
  }

  override fun deserialize(de: EntityInformation.Deserializer) {
  }

  override fun createDetachedEntity(parents: List<WorkspaceEntity>): WorkspaceEntity {
    return NuxtFolderEntity(nuxtFolderUrl, libraryFileUrls, entitySource) {
    }
  }

  override fun getRequiredParents(): List<Class<out WorkspaceEntity>> {
    val res = mutableListOf<Class<out WorkspaceEntity>>()
    return res
  }

  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (this.javaClass != other.javaClass) return false

    other as NuxtFolderEntityData

    if (this.entitySource != other.entitySource) return false
    if (this.nuxtFolderUrl != other.nuxtFolderUrl) return false
    if (this.libraryFileUrls != other.libraryFileUrls) return false
    return true
  }

  override fun equalsIgnoringEntitySource(other: Any?): Boolean {
    if (other == null) return false
    if (this.javaClass != other.javaClass) return false

    other as NuxtFolderEntityData

    if (this.nuxtFolderUrl != other.nuxtFolderUrl) return false
    if (this.libraryFileUrls != other.libraryFileUrls) return false
    return true
  }

  override fun hashCode(): Int {
    var result = entitySource.hashCode()
    result = 31 * result + nuxtFolderUrl.hashCode()
    result = 31 * result + libraryFileUrls.hashCode()
    return result
  }

  override fun hashCodeIgnoringEntitySource(): Int {
    var result = javaClass.hashCode()
    result = 31 * result + nuxtFolderUrl.hashCode()
    result = 31 * result + libraryFileUrls.hashCode()
    return result
  }
}

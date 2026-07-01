package com.mileway

import com.mileway.core.data.dao.DelegationDao
import com.mileway.core.data.model.db.DelegationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory fake for [DelegationDao] (P6.3) — lets JVM/Robolectric tests construct
 * `DelegationRepository`/`DelegationViewModel` without a Room instance, mirroring
 * [FakeMockAccountDao]'s shape.
 */
class FakeDelegationDao : DelegationDao {
    private val rows = MutableStateFlow<Map<String, DelegationEntity>>(emptyMap())

    override fun observeAll(): Flow<List<DelegationEntity>> = rows.map { it.values.sortedBy { row -> row.createdAt } }

    override suspend fun upsert(entity: DelegationEntity) {
        rows.value = rows.value + (entity.id to entity)
    }

    override suspend fun delete(id: String) {
        rows.value = rows.value - id
    }

    override suspend fun setActive(
        id: String,
        isActive: Boolean,
    ) {
        val existing = rows.value[id] ?: return
        rows.value = rows.value + (id to existing.copy(isActive = isActive))
    }
}

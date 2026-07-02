package com.mileway

import com.mileway.core.data.dao.ConnectedAccountDao
import com.mileway.core.data.model.db.ConnectedAccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory fake for [ConnectedAccountDao] (P6.6) — lets JVM/Robolectric tests construct
 * `ConnectedAccountsRepository`/`ConnectedAccountsViewModel` without a Room instance, mirroring
 * [FakeDelegationDao]'s shape.
 */
class FakeConnectedAccountDao : ConnectedAccountDao {
    private val rows = MutableStateFlow<Map<String, ConnectedAccountEntity>>(emptyMap())

    override fun observeAll(): Flow<List<ConnectedAccountEntity>> = rows.map { it.values.sortedBy { row -> row.providerName } }

    override suspend fun count(): Int = rows.value.size

    override suspend fun upsertAll(entities: List<ConnectedAccountEntity>) {
        rows.value = rows.value + entities.associateBy { it.id }
    }

    override suspend fun setConnected(
        id: String,
        isConnected: Boolean,
        updatedAtMs: Long,
    ) {
        val existing = rows.value[id] ?: return
        rows.value = rows.value + (id to existing.copy(isConnected = isConnected, updatedAtMs = updatedAtMs))
    }
}

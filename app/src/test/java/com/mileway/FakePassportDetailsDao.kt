package com.mileway

import com.mileway.core.data.dao.PassportDetailsDao
import com.mileway.core.data.model.db.PassportDetailsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory fake for [PassportDetailsDao] (P6.2) — lets JVM/Robolectric tests construct
 * `PassportDetailsRepository`/`PersonalDetailsViewModel` without a Room instance, mirroring
 * [FakeMockAccountDao]'s shape.
 */
class FakePassportDetailsDao : PassportDetailsDao {
    private val row = MutableStateFlow<PassportDetailsEntity?>(null)

    override fun observe(id: String): Flow<PassportDetailsEntity?> = row

    override suspend fun get(id: String): PassportDetailsEntity? = row.value

    override suspend fun upsert(entity: PassportDetailsEntity) {
        row.value = entity
    }
}

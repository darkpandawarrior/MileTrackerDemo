package com.mileway

import com.mileway.core.data.dao.VehicleDetailsDao
import com.mileway.core.data.model.db.VehicleDetailsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory fake for [VehicleDetailsDao] (P6.2) — lets JVM/Robolectric tests construct
 * `VehicleDetailsRepository`/`PersonalDetailsViewModel` without a Room instance, mirroring
 * [FakeMockAccountDao]'s shape.
 */
class FakeVehicleDetailsDao : VehicleDetailsDao {
    private val row = MutableStateFlow<VehicleDetailsEntity?>(null)

    override fun observe(id: String): Flow<VehicleDetailsEntity?> = row

    override suspend fun get(id: String): VehicleDetailsEntity? = row.value

    override suspend fun upsert(entity: VehicleDetailsEntity) {
        row.value = entity
    }
}

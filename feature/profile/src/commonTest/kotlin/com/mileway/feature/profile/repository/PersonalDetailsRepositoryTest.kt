package com.mileway.feature.profile.repository

import com.mileway.core.data.dao.PassportDetailsDao
import com.mileway.core.data.dao.VehicleDetailsDao
import com.mileway.core.data.model.db.PassportDetailsEntity
import com.mileway.core.data.model.db.VehicleDetailsEntity
import com.mileway.feature.profile.model.PassportDetails
import com.mileway.feature.profile.model.VehicleDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * P6.2: [VehicleDetailsRepository]/[PassportDetailsRepository] persist their singleton row via
 * [VehicleDetailsDao]/[PassportDetailsDao] — proven here against in-memory fake DAOs (same shape
 * `MockAccountRepositoryTest` uses), so a fresh repository instance backed by the same dao sees
 * whatever a prior instance wrote (i.e. it survives "process death" for a JVM test's purposes).
 */
class PersonalDetailsRepositoryTest {
    @Test
    fun `vehicle observe emits null before anything is saved`() =
        runTest {
            val repository = VehicleDetailsRepository(FakeVehicleDetailsDao())

            assertNull(repository.observe().first())
        }

    @Test
    fun `saved vehicle details survive a fresh repository instance backed by the same dao`() =
        runTest {
            val dao = FakeVehicleDetailsDao()
            val details = VehicleDetails(make = "Maruti", model = "Swift", registrationNumber = "MH12AB1234", fuelType = "Petrol", seatingCapacity = 5)
            VehicleDetailsRepository(dao).save(details)

            val restored = VehicleDetailsRepository(dao).observe().first()

            assertEquals(details, restored)
        }

    @Test
    fun `passport observe emits null before anything is saved`() =
        runTest {
            val repository = PassportDetailsRepository(FakePassportDetailsDao())

            assertNull(repository.observe().first())
        }

    @Test
    fun `saved passport details survive a fresh repository instance backed by the same dao`() =
        runTest {
            val dao = FakePassportDetailsDao()
            val details = PassportDetails(passportNumber = "P1234567", issuingCountry = "India", expiryDateMillis = 1_900_000_000_000L)
            PassportDetailsRepository(dao).save(details)

            val restored = PassportDetailsRepository(dao).observe().first()

            assertEquals(details, restored)
        }
}

private class FakeVehicleDetailsDao : VehicleDetailsDao {
    private val row = MutableStateFlow<VehicleDetailsEntity?>(null)

    override fun observe(id: String): Flow<VehicleDetailsEntity?> = row

    override suspend fun get(id: String): VehicleDetailsEntity? = row.value

    override suspend fun upsert(entity: VehicleDetailsEntity) {
        row.value = entity
    }
}

private class FakePassportDetailsDao : PassportDetailsDao {
    private val row = MutableStateFlow<PassportDetailsEntity?>(null)

    override fun observe(id: String): Flow<PassportDetailsEntity?> = row

    override suspend fun get(id: String): PassportDetailsEntity? = row.value

    override suspend fun upsert(entity: PassportDetailsEntity) {
        row.value = entity
    }
}

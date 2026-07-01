package com.mileway.feature.logging.repository

import com.mileway.core.data.dao.LogMilesDraftDao
import com.mileway.core.data.model.db.LogMilesDraftEntity
import com.mileway.feature.logging.repository.LogMilesDraftRepository.Companion.toDraftEntity
import com.mileway.feature.logging.ui.model.LocationEntry
import com.mileway.feature.logging.ui.model.LocationStop
import com.mileway.feature.logging.ui.model.PoiCategory
import com.mileway.feature.logging.viewmodel.LogMilesUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** In-memory [LogMilesDraftDao] mirroring `app`'s screenshot-Koin `FakeLogMilesDraftDao` (P5.1). */
private class FakeLogMilesDraftDao : LogMilesDraftDao {
    private val drafts = MutableStateFlow<Map<String, LogMilesDraftEntity>>(emptyMap())

    override fun getAllDrafts(): Flow<List<LogMilesDraftEntity>> = drafts.map { it.values.sortedByDescending { d -> d.updatedAt } }

    override suspend fun getDraftById(draftId: String): LogMilesDraftEntity? = drafts.value[draftId]

    override suspend fun upsertDraft(draft: LogMilesDraftEntity) {
        drafts.value = drafts.value + (draft.draftId to draft)
    }

    override suspend fun deleteDraftById(draftId: String) {
        drafts.value = drafts.value - draftId
    }

    override suspend fun deleteAllDrafts() {
        drafts.value = emptyMap()
    }
}

/**
 * P5.1 acceptance: "save a draft ... open it from history -> fields are restored", verified here
 * as a mapper round-trip (stops, isRoundTrip, vehicle key, journey/invoice dates, note, tagged
 * employees) through [LogMilesDraftRepository.toDraftEntity] -> Room (fake) -> [LogMilesDraftRepository.loadDraft].
 */
class LogMilesDraftRepositoryTest {
    private val stops =
        listOf(
            LocationStop(id = 0L, entry = LocationEntry("Origin", "Origin subtitle", 18.52, 73.85, PoiCategory.OFFICE)),
            LocationStop(id = 1L, entry = LocationEntry("Destination", "Destination subtitle", 18.55, 73.91, PoiCategory.CLIENT)),
        )

    private fun buildState(): LogMilesUiState =
        LogMilesUiState(
            stops = stops,
            isRoundTrip = true,
            calculatedDistanceKm = 12.34,
            distanceKm = 12.34,
            journeyDateMillis = 1_700_000_000_000L,
            invoiceDateMillis = 1_700_500_000_000L,
            logMilesNote = "Client visit",
            taggedEmployees = listOf("EMP001", "EMP002"),
        )

    @Test
    fun `save then load round-trips stops, round-trip flag, dates, note and employees`() =
        runTest {
            val dao = FakeLogMilesDraftDao()
            val repo = LogMilesDraftRepository(dao)
            val state = buildState()

            repo.save(state.toDraftEntity(draftId = "draft-1", createdAtMillis = 1_000L, nowMillis = 2_000L))
            val loaded = repo.loadDraft("draft-1")

            assertTrue(loaded != null)
            val restored = loaded.uiState
            assertEquals(stops, restored.stops)
            assertEquals(true, restored.isRoundTrip)
            assertEquals(1_700_000_000_000L, restored.journeyDateMillis)
            assertEquals(1_700_500_000_000L, restored.invoiceDateMillis)
            assertEquals("Client visit", restored.logMilesNote)
            assertEquals(listOf("EMP001", "EMP002"), restored.taggedEmployees)
        }

    @Test
    fun `loadDraft returns null for an unknown draft id`() =
        runTest {
            val repo = LogMilesDraftRepository(FakeLogMilesDraftDao())
            assertNull(repo.loadDraft("does-not-exist"))
        }

    @Test
    fun `toDraftEntity omits employees json when list is empty`() {
        val entity = buildState().copy(taggedEmployees = emptyList()).toDraftEntity("d", 1L, 2L)
        assertNull(entity.employeesJson)
    }

    @Test
    fun `toDraftEntity omits invoice date payload when null`() {
        val entity = buildState().copy(invoiceDateMillis = null).toDraftEntity("d", 1L, 2L)
        assertNull(entity.processorFormDataJson)
    }
}

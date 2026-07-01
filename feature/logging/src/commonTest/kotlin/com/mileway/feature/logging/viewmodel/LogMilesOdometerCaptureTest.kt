package com.mileway.feature.logging.viewmodel

import com.mileway.core.data.model.display.OdometerCaptureResult
import com.mileway.core.data.model.display.OdometerPurpose
import com.mileway.core.data.model.display.OdometerReadingSource
import com.mileway.core.data.model.network.ApprovedVehicle
import com.mileway.feature.logging.ui.model.LocationEntry
import com.mileway.feature.logging.ui.model.LocationStop
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [LogMilesUiState]'s odometer-capture gate (P5.3) — `OdometerCaptureResult`/
 * `OdometerPurpose`/`OdometerReadingSource` already existed in `core/data` unused; this covers the
 * new [LogMilesUiState.canProceedToStep2] gating and [LogMilesUiState.odometerValidationError].
 */
class LogMilesOdometerCaptureTest {
    private val vehicle = ApprovedVehicle(vehicleKey = "car", vehicleName = "Car", vehiclePricing = 12.0)
    private val stops =
        listOf(
            LocationStop(id = 1L, entry = LocationEntry(name = "A", subtitle = "", lat = 18.5, lng = 73.8)),
            LocationStop(id = 2L, entry = LocationEntry(name = "B", subtitle = "", lat = 18.6, lng = 73.9)),
        )

    private fun baseState() = LogMilesUiState(selectedVehicle = vehicle, stops = stops)

    private fun reading(
        purpose: OdometerPurpose,
        value: Int,
    ) = OdometerCaptureResult(
        purpose = purpose,
        imageUri = "file:///demo/odo.jpg",
        reading = value,
        source = OdometerReadingSource.MANUAL,
        captureTimeMs = 1_700_000_000_000L,
    )

    @Test
    fun `flag off - Step 2 gate unchanged by missing odometer readings`() {
        val state = baseState().copy(odometerCaptureEnabled = false)

        assertTrue(state.canProceedToStep2)
        assertNull(state.odometerValidationError)
    }

    @Test
    fun `flag on and no readings - Step 2 gate is blocked, no validation error yet`() {
        val state = baseState().copy(odometerCaptureEnabled = true)

        assertFalse(state.canProceedToStep2)
        assertNull(state.odometerValidationError)
    }

    @Test
    fun `flag on and end less than start - validation error and gate blocked`() {
        val state =
            baseState().copy(
                odometerCaptureEnabled = true,
                odometerStart = reading(OdometerPurpose.START, 1000),
                odometerEnd = reading(OdometerPurpose.END, 900),
            )

        assertEquals("End odometer reading must be greater than start", state.odometerValidationError)
        assertFalse(state.canProceedToStep2)
    }

    @Test
    fun `flag on and end equal to start - validation error (not just end less than start)`() {
        val state =
            baseState().copy(
                odometerCaptureEnabled = true,
                odometerStart = reading(OdometerPurpose.START, 1000),
                odometerEnd = reading(OdometerPurpose.END, 1000),
            )

        assertEquals("End odometer reading must be greater than start", state.odometerValidationError)
        assertFalse(state.canProceedToStep2)
    }

    @Test
    fun `flag on and valid complete capture - gate unblocked and no validation error`() {
        val state =
            baseState().copy(
                odometerCaptureEnabled = true,
                odometerStart = reading(OdometerPurpose.START, 1000),
                odometerEnd = reading(OdometerPurpose.END, 1050),
            )

        assertNull(state.odometerValidationError)
        assertTrue(state.canProceedToStep2)
    }

    @Test
    fun `toSubmitRequest carries odometerDistance as end minus start when captured`() {
        val state =
            baseState().copy(
                odometerCaptureEnabled = true,
                odometerStart = reading(OdometerPurpose.START, 1000),
                odometerEnd = reading(OdometerPurpose.END, 1050),
                distanceKm = 50.0,
            )

        assertEquals(50, state.toSubmitRequest().odometerDistance)
    }

    @Test
    fun `toSubmitRequest odometerDistance is null when capture was never done`() {
        val request = baseState().toSubmitRequest()

        assertNull(request.odometerDistance)
    }
}

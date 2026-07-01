package com.mileway.feature.logging.viewmodel

import com.mileway.core.data.model.network.ApprovedVehicle
import com.mileway.core.data.model.network.LogMilesService
import com.mileway.feature.logging.ui.model.LocationEntry
import com.mileway.feature.logging.ui.model.LocationStop
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for P5.5 — [LogMilesUiState.canProceedToStep2]'s multi-service gate. Previously
 * `ExpenseDetailsSection` rendered a hardcoded local service list disconnected from
 * `uiState.services`/`selectedService`, so a missing selection could never actually block
 * progression; this covers the new rule: with more than one service loaded, an explicit
 * selection is required, but a single (or not-yet-loaded) service list never blocks the gate.
 */
class LogMilesServiceSelectionTest {
    private val vehicle = ApprovedVehicle(vehicleKey = "car", vehicleName = "Car", vehiclePricing = 12.0)
    private val stops =
        listOf(
            LocationStop(id = 1L, entry = LocationEntry(name = "A", subtitle = "", lat = 18.5, lng = 73.8)),
            LocationStop(id = 2L, entry = LocationEntry(name = "B", subtitle = "", lat = 18.6, lng = 73.9)),
        )
    private val serviceA = LogMilesService(id = 1L, name = "Client Visit", glCode = "GL-1")
    private val serviceB = LogMilesService(id = 2L, name = "Office Travel", glCode = "GL-2")

    private fun baseState() = LogMilesUiState(selectedVehicle = vehicle, stops = stops)

    @Test
    fun `no services loaded - gate unaffected by missing selection`() {
        val state = baseState().copy(services = emptyList(), selectedService = null)

        assertTrue(state.canProceedToStep2)
    }

    @Test
    fun `single service - gate unaffected even without an explicit selection`() {
        val state = baseState().copy(services = listOf(serviceA), selectedService = null)

        assertTrue(state.canProceedToStep2)
    }

    @Test
    fun `multiple services and no selection - gate blocked`() {
        val state = baseState().copy(services = listOf(serviceA, serviceB), selectedService = null)

        assertFalse(state.canProceedToStep2)
    }

    @Test
    fun `multiple services with a selection - gate unblocked`() {
        val state = baseState().copy(services = listOf(serviceA, serviceB), selectedService = serviceA)

        assertTrue(state.canProceedToStep2)
    }
}

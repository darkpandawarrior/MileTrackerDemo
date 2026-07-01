package com.mileway.feature.logging.viewmodel

import com.mileway.core.data.model.network.ApprovedVehicle
import com.mileway.core.data.model.network.ExpenseSubmissionResponse
import com.mileway.core.data.model.network.PolicyViolation
import com.mileway.core.data.model.network.SubmissionStatus
import com.mileway.core.data.model.network.ViolationSeverity
import com.mileway.feature.logging.ui.model.LocationEntry
import com.mileway.feature.logging.ui.model.LocationStop
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for P5.4 — [ExpenseSubmissionResponse.needsViolationDialog]'s three-tier branch
 * (`REIMBURSABLE_ADJUSTED` / `POLICY_VIOLATION` / `HARD_STOP`) and the `force`/`violationRemarks`
 * fields [LogMilesUiState.toSubmitRequest] carries on a resolved resubmit. One test per severity
 * tier, per the P5.4 acceptance clause; the branch itself (which content composable renders) lives
 * in [com.mileway.feature.logging.ui.dialog.ViolationDialog]'s `when`, mirrored here at the level
 * that's unit-testable without standing up Compose.
 */
class LogMilesViolationResolutionTest {
    private val vehicle = ApprovedVehicle(vehicleKey = "car", vehicleName = "Car", vehiclePricing = 12.0)
    private val stops =
        listOf(
            LocationStop(id = 1L, entry = LocationEntry(name = "A", subtitle = "", lat = 18.5, lng = 73.8)),
            LocationStop(id = 2L, entry = LocationEntry(name = "B", subtitle = "", lat = 18.6, lng = 73.9)),
        )

    private fun baseState() = LogMilesUiState(selectedVehicle = vehicle, stops = stops, distanceKm = 12.0)

    // ── REIMBURSABLE_ADJUSTED tier ──────────────────────────────────────────────

    @Test
    fun `REIMBURSABLE_ADJUSTED response needs the violation dialog`() {
        val response =
            ExpenseSubmissionResponse(
                submissionStatus = SubmissionStatus.REIMBURSABLE_ADJUSTED,
                reimbursableAmount = 100.0,
            )

        assertTrue(response.needsViolationDialog())
    }

    @Test
    fun `accepting a REIMBURSABLE_ADJUSTED resubmit carries force with no remarks required`() {
        val request = baseState().toSubmitRequest(force = true, violationRemarks = "")

        assertEquals(true, request.force)
        assertNull(request.violationRemarks)
    }

    // ── POLICY_VIOLATION tier ───────────────────────────────────────────────────

    @Test
    fun `POLICY_VIOLATION response needs the violation dialog`() {
        val response =
            ExpenseSubmissionResponse(
                submissionStatus = SubmissionStatus.POLICY_VIOLATION,
                violations =
                    listOf(
                        PolicyViolation(
                            id = "max-distance-per-day",
                            title = "Daily distance limit exceeded",
                            message = "Only the first 10 km per day are reimbursable.",
                            severity = ViolationSeverity.VIOLATION,
                        ),
                    ),
            )

        assertTrue(response.needsViolationDialog())
    }

    @Test
    fun `resubmitting a POLICY_VIOLATION with remarks carries force and the remarks text`() {
        val request = baseState().toSubmitRequest(force = true, violationRemarks = "Client insisted on the detour")

        assertEquals(true, request.force)
        assertEquals("Client insisted on the detour", request.violationRemarks)
    }

    @Test
    fun `blank remarks map to null violationRemarks rather than an empty string`() {
        val request = baseState().toSubmitRequest(force = true, violationRemarks = "   ")

        assertNull(request.violationRemarks)
    }

    // ── HARD_STOP tier ───────────────────────────────────────────────────────────

    @Test
    fun `HARD_STOP response needs the violation dialog`() {
        val response =
            ExpenseSubmissionResponse(
                submissionStatus = SubmissionStatus.HARD_STOP,
                violations =
                    listOf(
                        PolicyViolation(
                            id = "max-trip-distance-hard-stop",
                            title = "Trip distance hard limit exceeded",
                            message = "Trips above 40 km cannot be submitted for reimbursement.",
                            severity = ViolationSeverity.HARDSTOP,
                        ),
                    ),
            )

        assertTrue(response.needsViolationDialog())
    }

    // ── Clean tier (no dialog) ────────────────────────────────────────────────────

    @Test
    fun `SUCCESS response with no violations does not need the violation dialog`() {
        val response = ExpenseSubmissionResponse(submissionStatus = SubmissionStatus.SUCCESS)

        assertFalse(response.needsViolationDialog())
    }

    // ── Default (non-resubmit) request shape unaffected ──────────────────────────

    @Test
    fun `a first-time submit request carries no force or remarks`() {
        val request = baseState().toSubmitRequest()

        assertNull(request.force)
        assertNull(request.violationRemarks)
    }
}

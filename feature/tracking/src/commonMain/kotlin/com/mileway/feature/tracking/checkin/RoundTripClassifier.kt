package com.mileway.feature.tracking.checkin

/**
 * Pure-Kotlin (no Android imports) round-trip auto-detection heuristic.
 *
 * A tracked journey is classified as a round trip when the start and end coordinates are
 * close together (within [proximityThresholdKm]) while the total tracked distance is
 * meaningfully larger than that closeness would suggest — i.e. the traveller went somewhere
 * and came back to (near) the same spot, rather than the trip simply being too short to have
 * gone anywhere. All computation is done locally; no network calls are made.
 */
object RoundTripClassifier {
    /** Minimum tracked distance (km) a trip must cover before "start ≈ end" counts as a round trip. */
    private const val MINIMUM_ROUND_TRIP_DISTANCE_KM = 0.5

    /**
     * Returns true when [startLat]/[startLng] and [endLat]/[endLng] are within
     * [proximityThresholdKm] of each other AND [totalDistanceKm] is at least
     * [MINIMUM_ROUND_TRIP_DISTANCE_KM] — i.e. the traveller returned near their starting point
     * after covering real distance, rather than never having moved.
     *
     * @param startLat            Trip start latitude in decimal degrees.
     * @param startLng            Trip start longitude in decimal degrees.
     * @param endLat              Trip end latitude in decimal degrees.
     * @param endLng              Trip end longitude in decimal degrees.
     * @param totalDistanceKm     Total tracked distance for the trip, in kilometres.
     * @param proximityThresholdKm Max start/end separation (km) still considered "the same place".
     */
    fun isRoundTrip(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        totalDistanceKm: Double,
        proximityThresholdKm: Double = 2.0,
    ): Boolean {
        if (totalDistanceKm < MINIMUM_ROUND_TRIP_DISTANCE_KM) return false
        val startEndDistanceKm = CheckInValidator.haversineMeters(startLat, startLng, endLat, endLng) / 1_000.0
        return startEndDistanceKm <= proximityThresholdKm
    }
}

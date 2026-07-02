package com.mileway.sharedwatch

/**
 * P3.3: a small, watchos-safe value model for a single completed trip — the subset a watch trip
 * list/detail screen renders. Deliberately the same shape as `feature:tracking`'s
 * `WatchFacade.TripSummary` (P1.2), duplicated rather than shared because `feature:tracking` is a
 * Compose module with no watchos target (see [WatchDomainFacade] doc) — this is the `:sharedWatch`
 * sibling, built on `core:data` types only.
 */
data class WatchTripSummary(
    val id: String,
    val label: String,
    val km: Double,
    val endMs: Long,
)

package com.mileway.feature.logging.ui.screens

import androidx.compose.runtime.Composable

/**
 * Opens the platform's photo picker for an optional local receipt attachment (P1.4). Returns a
 * callback that launches the picker; [onPicked] is invoked with the picked image's local URI/path
 * string once the user selects one (never invoked on cancel).
 *
 * Android: `ActivityResultContracts.PickVisualMedia` (system Photo Picker, no storage permission
 * needed) — the same picker `feature/tracking`'s `OdometerCameraScreen` already uses for its
 * gallery-pick affordance, reused here rather than inventing a new one.
 *
 * iOS: no UI hook exists yet for a native photo-picker launch from a `ComposeUIViewController`
 * host (mirrors the documented gap in `core/platform`'s `IosDocumentScanner`) — returns a truthful
 * no-op until that hook lands, rather than a fragile stand-in.
 */
@Composable
expect fun rememberReceiptAttachmentLauncher(onPicked: (String) -> Unit): () -> Unit

package com.mileway.feature.logging.ui.screens

import androidx.compose.runtime.Composable

/**
 * Opens the platform's file picker for a CSV/TSV bulk-import file (P2.4). Returns a callback that
 * launches the picker; [onPicked] is invoked with the picked file's raw text contents once the
 * user selects one (never invoked on cancel or a read failure).
 *
 * Android: `ActivityResultContracts.GetContent()` (system Storage Access Framework picker, no
 * storage permission needed), same picker family `ReceiptAttachmentLauncher` already uses for
 * image picking.
 *
 * iOS: no UI hook exists yet for a native document-picker launch from a `ComposeUIViewController`
 * host (mirrors the documented gap in [rememberReceiptAttachmentLauncher]) — returns a truthful
 * no-op until that hook lands, rather than a fragile stand-in.
 */
@Composable
expect fun rememberExpenseCsvImportLauncher(onPicked: (String) -> Unit): () -> Unit

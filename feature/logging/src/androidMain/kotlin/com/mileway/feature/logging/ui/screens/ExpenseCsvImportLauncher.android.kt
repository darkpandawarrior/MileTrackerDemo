package com.mileway.feature.logging.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android actual: the system Storage Access Framework picker (`GetContent`), same picker family
 * `ReceiptAttachmentLauncher` uses for image picking. Reads the picked document's bytes as UTF-8
 * text via `ContentResolver` — never invokes [onPicked] when nothing was picked or the read fails.
 */
@Composable
actual fun rememberExpenseCsvImportLauncher(onPicked: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            val text =
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                }.getOrNull()
            if (text != null) onPicked(text)
        }
    return remember(launcher) {
        { launcher.launch("*/*") }
    }
}

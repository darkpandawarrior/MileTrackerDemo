package com.mileway.wear.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.Typography

/**
 * P2.3: Wear Compose theme wrapper, mapping Mileway's Matrix palette onto
 * [androidx.wear.compose.material3.ColorScheme] + [Typography].
 *
 * [WearAppGraph][com.mileway.wear.WearAppGraph]'s doc comment is the reason this file hand-copies
 * hex values instead of depending on `core:ui`: `core:ui` is the Compose *Multiplatform* theming
 * module ([androidx.compose.material3.ColorScheme], a different type from Wear's own
 * `androidx.wear.compose.material3.ColorScheme]`) — Wear renders with `androidx.wear.compose`, its
 * own design system, and must never pull the phone/iOS CMP theming module onto its classpath.
 *
 * The literal hex values below are copied 1:1 from the Matrix scheme (`MilewayThemeVariant.MATRIX`,
 * the app default) so the watch stays visually on-brand with the phone:
 *  - Accent ramp + canvas/surface ramp: `core/ui/.../theme/MilewayThemes.kt` (`MatrixSpec`).
 *  - Semantic status colours: `core/ui/.../theme/DesignTokens.kt` (`DesignTokens.StatusColors`),
 *    which itself documents being "kept in lock-step" with the Matrix scheme — the same discipline
 *    applies here. If the Matrix palette changes, update both places.
 * Only the phone's curated Matrix theme is mirrored — Wear OS doesn't expose the phone's in-app
 * theme picker (Amoled/Ion/Daybreak), so there's exactly one Wear scheme for now.
 */
private object WearMatrixPalette {
    val canvas = Color(0xFF010701)
    val surface = Color(0xFF040C06)
    val surfaceCard = Color(0xFF080F0A)
    val surfaceRaised = Color(0xFF0C1510)
    val surfaceHighest = Color(0xFF111C14)
    val border = Color(0xFF1C3522)
    val text = Color(0xFFB8FFCC)
    val textMuted = Color(0xFF3A6645)
    val accent = Color(0xFF00FF41)
    val accentDim = Color(0xFF00CC34)
    val onAccent = Color(0xFF000000)
    val accentContainer = Color(0xFF00280E)
    val onAccentContainer = Color(0xFF7FFFAA)
    val warning = Color(0xFFFFCC00)
    val danger = Color(0xFFFF4455)
    val info = Color(0xFF33AAFF)
}

/** The Matrix palette mapped onto Wear Compose Material3's colour roles. */
private val wearMatrixColorScheme: ColorScheme by lazy {
    ColorScheme(
        primary = WearMatrixPalette.accent,
        primaryDim = WearMatrixPalette.accentDim,
        primaryContainer = WearMatrixPalette.accentContainer,
        onPrimary = WearMatrixPalette.onAccent,
        onPrimaryContainer = WearMatrixPalette.onAccentContainer,
        secondary = WearMatrixPalette.accentDim,
        secondaryDim = WearMatrixPalette.accentDim,
        secondaryContainer = WearMatrixPalette.surfaceRaised,
        onSecondary = WearMatrixPalette.onAccent,
        onSecondaryContainer = WearMatrixPalette.text,
        tertiary = WearMatrixPalette.info,
        tertiaryDim = WearMatrixPalette.info,
        tertiaryContainer = WearMatrixPalette.surfaceRaised,
        onTertiary = WearMatrixPalette.canvas,
        onTertiaryContainer = WearMatrixPalette.text,
        surfaceContainerLow = WearMatrixPalette.surface,
        surfaceContainer = WearMatrixPalette.surfaceCard,
        surfaceContainerHigh = WearMatrixPalette.surfaceRaised,
        onSurface = WearMatrixPalette.text,
        onSurfaceVariant = WearMatrixPalette.textMuted,
        outline = WearMatrixPalette.border,
        outlineVariant = WearMatrixPalette.border.copy(alpha = 0.7f),
        background = WearMatrixPalette.canvas,
        onBackground = WearMatrixPalette.text,
        error = WearMatrixPalette.danger,
        errorDim = WearMatrixPalette.danger,
        errorContainer = WearMatrixPalette.danger.copy(alpha = 0.22f),
        onError = WearMatrixPalette.canvas,
        onErrorContainer = WearMatrixPalette.danger,
    )
}

/**
 * Wear Compose typography for the "mono for data" aesthetic (see `core/ui/.../theme/Type.kt`):
 * numeric readouts (distance, speed, duration) use a monospaced family so digits stay tabular on
 * the small round display. Built from Wear Compose's default [Typography] so every role Mileway's
 * screens don't explicitly override (arc labels, button copy, etc.) keeps its Wear-tuned metrics.
 */
private val wearMilewayTypography: Typography by lazy {
    val default = Typography()
    default.copy(
        displayLarge = default.displayLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
        displayMedium = default.displayMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
        displaySmall = default.displaySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold),
        titleLarge = default.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold),
        titleMedium = default.titleMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium),
        titleSmall = default.titleSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium),
        numeralExtraLarge = default.numeralExtraLarge.copy(fontFamily = FontFamily.Monospace),
        numeralLarge = default.numeralLarge.copy(fontFamily = FontFamily.Monospace),
        numeralMedium = default.numeralMedium.copy(fontFamily = FontFamily.Monospace),
        numeralSmall = default.numeralSmall.copy(fontFamily = FontFamily.Monospace),
        numeralExtraSmall = default.numeralExtraSmall.copy(fontFamily = FontFamily.Monospace),
    )
}

/**
 * Wraps [content] in Wear Compose Material3's [MaterialTheme] using the Matrix palette above.
 * Every Wear screen/tile-preview/complication-preview roots itself here instead of the bare
 * [MaterialTheme] default, so the watch surfaces match the phone's signature phosphor-green look.
 */
@Composable
fun WearMilewayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = wearMatrixColorScheme,
        typography = wearMilewayTypography,
        content = content,
    )
}

/**
 * Acceptance for P2.3: a preview renders the Matrix palette on a round-watch preview. Visual-only —
 * this task is explicitly unit-free (see PLAN_V23 P2.3 acceptance).
 */
// Devices.WEAR_OS_SMALL_ROUND is deprecated in favour of androidx.wear:wear-tooling-preview's
// WearDevices.SMALL_ROUND — not worth a new dependency for a preview-only device string constant
// (CLAUDE.md: no dependencies beyond what a task genuinely needs); the deprecated constant still
// resolves correctly.
// UnusedPrivateMember: only called by the Compose/Android Studio preview renderer via
// reflection on the @Preview annotation, never from Kotlin call sites — a known detekt false
// positive for private @Preview composables (no detekt-compose ruleset in this repo to exempt it).
@Suppress("DEPRECATION", "UnusedPrivateMember")
@Preview(name = "Wear Matrix theme", device = Devices.WEAR_OS_SMALL_ROUND, showBackground = true)
@Composable
private fun WearMilewayThemePreview() {
    WearMilewayTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Mileway",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

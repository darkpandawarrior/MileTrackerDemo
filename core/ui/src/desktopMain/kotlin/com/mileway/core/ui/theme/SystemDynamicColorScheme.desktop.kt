package com.mileway.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/** Desktop has no wallpaper-derived dynamic colour source; always falls back to the curated theme. */
@Composable
actual fun systemDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

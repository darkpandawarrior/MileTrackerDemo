package com.mileway.wear

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.github.takahirom.roborazzi.captureRoboImage
import com.mileway.core.data.model.display.SurfaceSnapshot
import com.mileway.wear.theme.WearMilewayTheme
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * showcase/Wear.1: Roborazzi host-render of the Ember Wear dashboard + trip list over a
 * deterministic mock [WearRootUiState] — mirrors `app/src/test/.../ScreenshotGalleryTest.kt`'s
 * pattern (Robolectric + Roborazzi, no device/emulator). Output: docs/screenshots/wear_*.png.
 */
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class, qualifiers = "w227dp-h227dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WearScreenshotGalleryTest {

    companion object {
        private val screenshotsDir: File by lazy {
            val moduleDir = File(System.getProperty("user.dir") ?: ".")
            val repoRoot = if (moduleDir.name == "wear") moduleDir.parentFile else moduleDir
            File(repoRoot, "docs/screenshots").also { it.mkdirs() }
        }

        // Record-only documentation screenshots written to docs/ (the README gallery), matching
        // ScreenshotGalleryTest's app-module convention.
        @org.junit.BeforeClass
        @JvmStatic
        fun setup() {
            System.setProperty("roborazzi.test.record", "true")
        }
    }

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun wearDashboard() {
        composeRule.setContent {
            WearMilewayTheme {
                AppScaffold {
                    val listState = rememberScalingLazyListState()
                    ScreenScaffold(scrollState = listState) {
                        WearDashboard(
                            uiState =
                                WearRootUiState(
                                    todayDistanceKm = 12.4,
                                    weekDistanceKm = 58.7,
                                    isTracking = true,
                                    weekGoalKm = 100.0,
                                    weekGoalProgress = 0.587f,
                                    trips = mockTrips(),
                                ),
                            listState = listState,
                            onTripsClick = {},
                        )
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage(File(screenshotsDir, "wear_dashboard.png").absolutePath)
    }

    @Test
    fun wearTripList() {
        composeRule.setContent {
            WearMilewayTheme {
                AppScaffold {
                    val listState = rememberScalingLazyListState()
                    ScreenScaffold(scrollState = listState) {
                        TripListScreen(
                            trips = mockTrips(),
                            listState = listState,
                            onTripClick = {},
                        )
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage(File(screenshotsDir, "wear_trip_list.png").absolutePath)
    }

    // MileageTileService renders a ProtoLayout tile, not a Composable — this approximates its
    // actual content (today's distance label via WearPresentation.toTodayDistanceLabel + the
    // "Mileway" app label) as a Compose render so the Ember visual is documented without a
    // ProtoLayout renderer on the JVM.
    @Test
    fun wearTile() {
        composeRule.setContent {
            WearMilewayTheme {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = WearPresentation.toTodayDistanceLabel(SurfaceSnapshot(todayDistanceKm = 12.4)),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(text = "Mileway", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage(File(screenshotsDir, "wear_tile.png").absolutePath)
    }

    private fun mockTrips() =
        listOf(
            TripListItemUi(id = "t1", label = "Commute", km = 12.4, endMs = 1_700_000_000_000L),
            TripListItemUi(id = "t2", label = "Airport pickup", km = 42.1, endMs = 1_700_000_000_000L - 86_400_000L),
            TripListItemUi(id = "t3", label = "Warehouse run", km = 9.8, endMs = 1_700_000_000_000L - 3 * 86_400_000L),
        )
}

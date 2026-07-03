// G11: Android-only Glance home-screen widget. Uses the shared android.library convention (com.android.library
// + Compose compiler + compose=true) so the Glance @Composable content compiles. Consumes the platform-neutral
// SurfaceSnapshot model/producer from :core:data (the producer already exists; this is the missing consumer).
plugins {
    id("mileway.android.library")
}

android {
    namespace = "com.mileway.widget"

    // showcase/Widget.1: Robolectric needs the merged resources to inflate the RemoteViews layouts
    // Glance generates; src/test/res/values/themes.xml stubs the @style/Theme.Mileway reference
    // pulled in transitively from :feature:tracking's manifest (mirrors :app/:wear's testOptions).
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.glance.appwidget)
    implementation(project(":core:data"))
    // P6.2: WatchFacade (start/stop-tracking) for the widget's quick-start/stop button.
    implementation(project(":feature:tracking"))
    // KoinPlatform.getKoin() — same framework-instantiated-component lookup pattern
    // WearTrackingCommandService/MileageTileService use; a GlanceAppWidgetReceiver runs in-process
    // on Android, so the app's already-started Koin graph is reachable here.
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)

    // showcase/Widget.1: Roborazzi host-render of the Glance content's RemoteViews (mirrors :wear's wiring).
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi.core)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
}

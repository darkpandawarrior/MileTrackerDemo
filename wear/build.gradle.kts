import org.gradle.api.artifacts.component.ModuleComponentIdentifier

plugins {
    id("mileway.android.application")
    id("mileway.test")
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.mileway.wear"
    defaultConfig {
        applicationId = "com.mileway.wear"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    // P2.1: mirror :app's maps-flavor split so the Wear app builds the same FOSS/proprietary story
    // (P2.2 adds the FOSS-purity guard on top of this dimension).
    flavorDimensions += "tier"
    productFlavors {
        create("gms") {
            dimension = "tier"
        }
        create("noGms") {
            dimension = "tier"
        }
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:platform"))
    implementation(project(":feature:tracking"))
    implementation(project(":stub"))

    // Compose for Wear OS.
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.material3)
    implementation(libs.wear.compose.navigation)
    implementation(libs.activity.compose)

    // Tile / complication / ongoing-activity surfaces (already present before this task).
    implementation(libs.wear.protolayout)
    implementation(libs.wear.tiles)
    implementation(libs.wear.tiles.material)
    implementation(libs.wear.complications.datasource)
    implementation(libs.wear.ongoing)
    implementation(libs.core.ktx)
    implementation("com.google.guava:guava:33.4.0-android")

    // Koin — WearAppGraph boots the same coreDataModule/trackingModule/stubModule graph the phone uses.
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
}

// ─── P2.2, FOSS-purity dependency guard for the wear noGms flavor ───────────────────────────────────
// Mirrors :app's verifyNoGmsDependencyPrefixes (FLFD.2): fails if a GMS/Play/Firebase coordinate
// leaks onto noGmsReleaseRuntimeClasspath. The wear surfaces (tile/complication/ongoing-activity)
// are all AndroidX (androidx.wear.*), never play-services — but :wear depends on :core:platform,
// whose androidMain unconditionally implements the SAME pre-existing FusedLocation + ML Kit OCR
// prime-feature chain :app already allowlists (LocationTracker / TextRecognizer, not flavor-split
// at the core:platform level — out of P2.2 scope to change). Allowlist mirrors :app's exactly so
// this guard's real job — catching a FUTURE GMS leak, e.g. an unguarded play-services-wearable ref
// outside wear/src/gms (P2.9/P2.10) — still works.
// Inside afterEvaluate so the AGP-created variant configuration exists when we look it up.
afterEvaluate {
    val forbiddenPrefixes =
        listOf(
            "com.google.android.gms",
            "com.google.android.play",
            "com.google.firebase",
            "com.google.maps.android",
            "com.android.installreferrer",
        )
    val allowlist =
        setOf(
            // Pre-existing FusedLocation chain (core:platform AndroidLocationTracker).
            "com.google.android.gms:play-services-location",
            "com.google.android.gms:play-services-base",
            "com.google.android.gms:play-services-basement",
            "com.google.android.gms:play-services-tasks",
            // Pre-existing ML Kit OCR (core:platform AndroidTextRecognizer / doc scanner).
            "com.google.android.gms:play-services-mlkit-document-scanner",
            "com.google.android.gms:play-services-mlkit-text-recognition",
            "com.google.android.gms:play-services-mlkit-text-recognition-common",
            // Transitive Firebase infra pulled by the play-services libs above (NOT messaging/analytics/crashlytics).
            "com.google.firebase:firebase-annotations",
            "com.google.firebase:firebase-components",
            "com.google.firebase:firebase-encoders",
            "com.google.firebase:firebase-encoders-json",
        )
    val verifyTask =
        tasks.register("verifyNoGmsDependencyPrefixes") {
            group = "verification"
            description = "Fails if a proprietary dependency leaks into the wear noGms (F-Droid) release classpath."
            // Resolves the variant classpath at execution time via the component graph (not artifacts, which
            // would hit variant-attribute ambiguity for project deps).
            notCompatibleWithConfigurationCache("Resolves the noGmsReleaseRuntimeClasspath component graph")
            doLast {
                val deps =
                    configurations.getByName("noGmsReleaseRuntimeClasspath")
                        .incoming.resolutionResult.allComponents
                        .mapNotNull { it.id as? ModuleComponentIdentifier }
                        .map { "${it.group}:${it.module}" }
                val violations =
                    deps
                        .filter { dep -> forbiddenPrefixes.any { dep.startsWith(it) } && dep !in allowlist }
                        .distinct()
                if (violations.isNotEmpty()) {
                    throw GradleException(
                        "wear noGms (F-Droid) release leaks proprietary dependencies: $violations",
                    )
                }
            }
        }
    tasks.named("check").configure { dependsOn(verifyTask) }
    tasks.matching { it.name == "assembleNoGmsRelease" }.configureEach { dependsOn(verifyTask) }
}

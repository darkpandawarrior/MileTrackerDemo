import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for a Kotlin Multiplatform library targeting Android + iOS.
 *
 * Applies the Kotlin Multiplatform + AGP KMP-library plugins and declares the iOS targets, so each
 * consuming module only declares its `android { namespace/compileSdk/minSdk }` block + source sets.
 */
class MilewayKmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.kotlin.multiplatform.library")
        }
        extensions.configure<KotlinMultiplatformExtension> {
            iosArm64()
            iosSimulatorArm64()
        }
    }
}

/**
 * Opt-in convention plugin adding `watchos*` targets on top of [MilewayKmpLibraryConventionPlugin].
 *
 * Deliberately a *separate* plugin id (mirroring how `mileway.kmp.compose` layers on
 * `mileway.kmp.library`) rather than a flag on the base plugin, so watchOS targets stay an explicit
 * per-module opt-in: `core:ui`/Compose and Wear/phone-only modules apply only `mileway.kmp.library`
 * and never see `watchos*` (PLAN_V23 §2/§6 — SQLCipher-backed/Compose modules must not compile for
 * watchOS). Also applies `applyDefaultHierarchyTemplate()` so the `apple`/`native` intermediate
 * source sets exist for `watchos*` to share `actual`s with `ios*` via `appleMain`.
 */
class MilewayKmpLibraryWatchosConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("mileway.kmp.library")
        extensions.configure<KotlinMultiplatformExtension> {
            applyDefaultHierarchyTemplate()
            watchosArm64()
            watchosSimulatorArm64()
            watchosDeviceArm64()
        }
    }
}

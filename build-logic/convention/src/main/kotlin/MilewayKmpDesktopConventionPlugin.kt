import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Opt-in convention plugin adding a `jvm("desktop")` target on top of [MilewayKmpLibraryConventionPlugin]
 * (PLAN_V23 Phase D — Compose Desktop dashboard).
 *
 * Mirrors [MilewayKmpLibraryWatchosConventionPlugin]: a *separate* plugin id rather than a flag on the
 * base plugin, so the desktop target stays an explicit per-module opt-in (only the `core:{common,data,
 * platform,ui}` graph a dashboard-only app needs, per Option b — NOT `feature:tracking`/maps). Must be
 * `jvm("desktop")` (the Compose-desktop-attributed target), not a bare `jvm()`, or AndroidX
 * lifecycle/Compose variant resolution fails with misleading "unresolved reference" errors.
 */
class MilewayKmpDesktopConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("mileway.kmp.library")
        extensions.configure<KotlinMultiplatformExtension> {
            applyDefaultHierarchyTemplate()
            jvm("desktop")
        }
    }
}

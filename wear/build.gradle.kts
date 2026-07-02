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

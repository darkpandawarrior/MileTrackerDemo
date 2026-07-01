plugins {
    id("mileway.cmp.feature")
}

kotlin {
    android {
        namespace = "com.mileway.feature.logging"
        compileSdk = 37
        minSdk = 30
        // Enable JVM host execution of commonTest so catalog/validator tests run in the gradle gate.
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:data"))
            implementation(project(":core:network"))
            implementation(project(":core:ui"))
            implementation(project(":feature:tracking"))
            // P1.6: reuses PolicyMockData's tiered policy engine for expense-amount validation.
            implementation(project(":stub"))
            // P1.4: renders an optional local receipt photo on the entry form + detail screen.
            implementation(libs.coil3.compose)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

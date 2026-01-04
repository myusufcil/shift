rootProject.name = "Shift"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Old composeApp (will migrate later)
include(":composeApp")

// Shift app module
include(":app")

// Core shared modules
include(":core:common")
include(":core:designsystem")
include(":core:domain")
include(":core:data")
include(":core:database")

// Feature modules - Habits
include(":feature:habits:domain")
include(":feature:habits:data")
include(":feature:habits:presentation")

// Feature modules - Statistics
include(":feature:statistics:domain")
include(":feature:statistics:data")
include(":feature:statistics:presentation")

// Feature modules - Settings
include(":feature:settings:presentation")

// Feature modules - Onboarding
include(":feature:onboarding:presentation")
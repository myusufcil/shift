plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ShiftApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Core modules
            implementation(project(":core:common"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:domain"))
            implementation(project(":core:data"))
            implementation(project(":core:database"))

            // Feature modules
            implementation(project(":feature:onboarding:presentation"))
            implementation(project(":feature:habits:presentation"))
            implementation(project(":feature:habits:domain"))
            implementation(project(":feature:habits:data"))
            implementation(project(":feature:statistics:presentation"))
            implementation(project(":feature:settings:presentation"))

            // Navigation
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tab.navigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.koin)

            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            // Firebase
            implementation(libs.firebase.auth)
            implementation(libs.firebase.common)

            // RevenueCat
            implementation(libs.revenuecat.purchases)
            implementation(libs.revenuecat.purchases.ui)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.androidx.work.runtime)
            implementation(libs.androidx.splashscreen)
        }
    }
}

android {
    namespace = "com.cil.shift"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.cil.shift"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

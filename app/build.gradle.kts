@file:Suppress("SpellCheckingInspection")

plugins {
    alias(libs.plugins.maplib.android.application)
    alias(libs.plugins.googleServices)
}

android {
    namespace = "com.omricat.maplibrarian"
    defaultConfig {
        applicationId = "com.omricat.maplibrarian"
        versionCode = 1
        versionName = "0.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resourceConfigurations += "en" // Only keep languages supported so to trim down FirebaseUI
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    lint {
        warningsAsErrors = false
        abortOnError = false
        disable += listOf("GradleDependency")
    }

    buildFeatures { viewBinding = true }

    testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
    implementation(projects.core)

    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.playServices)

    implementation(androidx.appCompat)
    implementation(androidx.coreKtx)

    implementation(androidx.activityKtx)
    implementation(androidx.lifecycle.viewModelKtx)
    implementation(androidx.lifecycle.commonJava8)

    compileOnly(androidx.annotation)

    implementation(androidx.constraintLayout)
    implementation(androidx.recylerView)
    implementation(libs.material)

    implementation(androidx.dataStore.preferences)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestoreKtx)
    implementation(libs.firebase.authKtx)

    implementation(libs.workflow.ui.core.android)
    implementation(libs.workflow.ui.container.android)

    implementation(libs.kotlinResult)
    implementation(libs.kotlinResult.coroutines)

    implementation(libs.jakeWharton.timber)

    debugImplementation(libs.beagle.drawer)
    debugImplementation(libs.jakeWharton.processPhoenix)
}

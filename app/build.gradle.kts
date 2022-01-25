@file:Suppress("SpellCheckingInspection")

import com.omricat.gradle.BuildVersions

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
}

android {
    val buildVersions: BuildVersions by rootProject.extra
    compileSdk = buildVersions.compileSdk

    defaultConfig {
        minSdk = buildVersions.minSdk
        targetSdk = buildVersions.targetSdk

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
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    sourceSets.forEach { srcSet ->
        srcSet.java.srcDir("src/${srcSet.name}/kotlin")
    }

    lint {
        isWarningsAsErrors = false
        isAbortOnError = false
        disable("GradleDependency")
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {

    implementation(project(":core"))

    implementation(KotlinX.coroutines.core)
    implementation(KotlinX.coroutines.android)
    implementation(KotlinX.coroutines.playServices)

    implementation(AndroidX.appCompat)
    implementation(AndroidX.core.ktx)

    implementation(AndroidX.activity.ktx)
    implementation(AndroidX.lifecycle.viewModelKtx)
    implementation(AndroidX.lifecycle.commonJava8)

    compileOnly(AndroidX.annotation)

    implementation(AndroidX.constraintLayout)
    implementation(AndroidX.recyclerView)
    implementation(Google.android.material)

    implementation(platform(Firebase.bom))
    implementation(Firebase.cloudFirestoreKtx)
    implementation(Firebase.authenticationKtx)

    fun workflow(artifact: String) = "com.squareup.workflow1:workflow-$artifact:_"
    implementation(workflow("ui-core-android"))
    implementation(workflow("ui-backstack-android"))

    implementation("com.michael-bull.kotlin-result:kotlin-result:_")

    implementation(JakeWharton.timber)

    compileOnly("dev.ahmedmourad.nocopy:nocopy-annotations:_")

    testImplementation(Testing.kotest.runner.junit5)
    testImplementation(Testing.kotest.assertions.core)

    testImplementation(workflow("testing-jvm"))

    androidTestImplementation(AndroidX.test.ext.junit)
    androidTestImplementation(AndroidX.test.rules)
    androidTestImplementation(AndroidX.test.espresso.core)
}

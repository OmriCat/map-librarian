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
            versionNameSuffix = "-debug"
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
        unitTests {
            isIncludeAndroidResources = true
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
    implementation("com.michael-bull.kotlin-result:kotlin-result-coroutines:_")

    implementation(JakeWharton.timber)

    compileOnly("dev.ahmedmourad.nocopy:nocopy-annotations:_")

    testImplementation(KotlinX.coroutines.test)

    testImplementation(Testing.kotest.assertions.core)
    testImplementation(Testing.robolectric)
    testImplementation(Testing.junit4)

    testImplementation(platform("org.testcontainers:testcontainers-bom:_"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:gcloud")
    testImplementation("org.slf4j:slf4j-simple:_") // for testcontainers logs

    testImplementation(AndroidX.test.ext.junitKtx)
    testImplementation(AndroidX.test.coreKtx)

    testImplementation(workflow("testing-jvm"))

    androidTestImplementation(AndroidX.test.ext.junit)
    androidTestImplementation(AndroidX.test.rules)
    androidTestImplementation(AndroidX.test.espresso.core)
}

@file:Suppress("SpellCheckingInspection")

import com.omricat.gradle.AndroidBuildToolVersions

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    val androidBuildVersions: AndroidBuildToolVersions by rootProject.extra
    compileSdkVersion(androidBuildVersions.compileSdk)

    defaultConfig {
        minSdkVersion(androidBuildVersions.minSdk)
        targetSdkVersion(androidBuildVersions.targetSdk)

        applicationId = "com.omricat.maplibrarian"
        versionCode = 1
        versionName = "0.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resConfigs("en") // Only keep languages supported so to trim down FirebaseUI
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
        }
    }

    sourceSets.forEach { srcSet ->
        srcSet.java.srcDir("src/${srcSet.name}/kotlin")
    }

    lintOptions {
        isWarningsAsErrors = true
        isAbortOnError = true
        disable("GradleDependency")
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    val coroutinesVersion = "1.4.1"
    fun coroutines(artifact: String) =
        "org.jetbrains.kotlinx:kotlinx-coroutines-$artifact:$coroutinesVersion"

    implementation(KotlinX.coroutines.core)
    implementation(KotlinX.coroutines.android)

    implementation(AndroidX.appCompat)
    implementation(AndroidX.core.ktx)

    implementation(AndroidX.activityKtx)
    implementation(AndroidX.fragmentKtx)
    implementation(AndroidX.lifecycle.viewModelKtx)
    implementation(AndroidX.lifecycle.runtimeKtx)
    implementation(AndroidX.lifecycle.commonJava8)
    implementation(AndroidX.annotation)

    implementation(AndroidX.constraintLayout)
    implementation(AndroidX.recyclerView)
    implementation(Google.android.material)

    implementation(platform(Firebase.bom))
    implementation(Firebase.cloudFirestoreKtx)
    implementation("com.google.firebase:firebase-auth-ktx")

    implementation("com.firebaseui:firebase-ui-firestore:_")
    implementation("com.firebaseui:firebase-ui-auth:_")

    fun workflow(artifact: String) = "com.squareup.workflow1:workflow-$artifact:_"
    implementation(workflow("ui-core-android"))
    implementation(workflow("ui-backstack-android"))

    implementation("com.michael-bull.kotlin-result:kotlin-result:_")


    testImplementation(Testing.kotest.runner.junit5)
    testImplementation(Testing.kotest.assertions.core)
    testImplementation(Testing.kotest.core)

    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}

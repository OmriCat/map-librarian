plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)

        applicationId = "com.omricat.maplibrarian"
        versionCode = 1
        versionName = "0.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }

    sourceSets.forEach { srcSet ->
        srcSet.java.srcDir("src/${srcSet.name}/kotlin")
    }

    lintOptions {
        isWarningsAsErrors = true
        isAbortOnError = true
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("com.android.support.constraint:constraint-layout:1.1.3")
    implementation("androidx.core:core-ktx:1.2.0")

    implementation("com.google.firebase:firebase-auth:19.3.1")


    implementation("androidx.fragment:fragment-ktx:1.2.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.1.0")

    testImplementation("junit:junit:4.13")

    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}

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
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    val coroutinesVersion = "1.4.1"
    fun coroutines(artifact: String) = "org.jetbrains.kotlinx:kotlinx-coroutines-$artifact:$coroutinesVersion"

    implementation(KotlinX.coroutines.core)
    implementation(KotlinX.coroutines.android)

    implementation(AndroidX.appCompat)
    implementation(AndroidX.core.ktx)

    implementation(AndroidX.activityKtx)
    implementation(AndroidX.fragmentKtx)
    implementation(AndroidX.lifecycle.viewModelKtx)
    implementation(AndroidX.lifecycle.runtimeKtx)

    implementation(AndroidX.constraintLayout)
    implementation(AndroidX.recyclerView)
    implementation(Google.android.material)

    implementation(platform(Firebase.bom))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")


    implementation("com.firebaseui:firebase-ui-firestore:_")
    implementation("com.firebaseui:firebase-ui-auth:_")

    testImplementation("junit:junit:4.13")

    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}

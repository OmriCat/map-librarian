import com.omricat.gradle.BuildVersions

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.omricat.maplibrarian.firebase"

    val buildVersions: BuildVersions by rootProject.extra
    compileSdk = buildVersions.compileSdk

    defaultConfig {
        minSdk = buildVersions.minSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets.forEach { srcSet ->
        srcSet.java.srcDir("src/${srcSet.name}/kotlin")
    }

    kotlin {
        explicitApi()
    }

}

dependencies {
    implementation(projects.core)

    implementation(KotlinX.coroutines.core)
    implementation(KotlinX.coroutines.android)
    implementation(KotlinX.coroutines.playServices)

    implementation(platform(Firebase.bom))
    implementation(Firebase.cloudFirestoreKtx)
    implementation(Firebase.authenticationKtx)

    implementation("com.michael-bull.kotlin-result:kotlin-result:_")
    implementation("com.michael-bull.kotlin-result:kotlin-result-coroutines:_")

    implementation(JakeWharton.timber)

    testImplementation(KotlinX.coroutines.test)

    testImplementation(Testing.robolectric)
    testImplementation(Testing.junit4)

    testImplementation(projects.firebaseEmulatorContainer)
    testImplementation("org.slf4j:slf4j-simple:_") // for testcontainers logs

    testImplementation(AndroidX.test.ext.junit.ktx)
    testImplementation(AndroidX.test.coreKtx)

    androidTestImplementation(AndroidX.test.ext.junit)
    androidTestImplementation(AndroidX.test.rules)

    androidTestImplementation(KotlinX.coroutines.test)
}

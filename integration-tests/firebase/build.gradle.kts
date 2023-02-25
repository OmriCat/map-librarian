import com.android.build.api.dsl.ManagedVirtualDevice
import com.omricat.gradle.BuildVersions

plugins {
    id("com.android.test")
    kotlin("android")
}

android {
    namespace = "com.omricat.maplibrarian.testing.debug"
    targetProjectPath = ":app"

    val buildVersions: BuildVersions by rootProject.extra
    compileSdk = buildVersions.compileSdk

    defaultConfig {
        minSdk = buildVersions.minSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes.named("debug") {
        isDebuggable = true
    }

    sourceSets.forEach { srcSet ->
        srcSet.java.srcDir("src/${srcSet.name}/kotlin")
    }

    testOptions {
        managedDevices {
            devices {
                maybeCreate<ManagedVirtualDevice>("nexus5api27").apply {
                    device = "Nexus 5"
                    apiLevel = 27
                    systemImageSource = "google"
                }
                maybeCreate<ManagedVirtualDevice>("nexus5api30").apply {
                    device = "Nexus 5"
                    apiLevel = 30
                    systemImageSource = "google-atd"
                }
            }
        }
    }
}

dependencies {
    implementation(projects.core)
    implementation(projects.app)

    implementation(KotlinX.coroutines.core)
    implementation(KotlinX.coroutines.android)

    implementation(platform(Firebase.bom))
    implementation(Firebase.cloudFirestoreKtx)
    implementation(Firebase.authenticationKtx)

    implementation("com.michael-bull.kotlin-result:kotlin-result:_")
    implementation("com.michael-bull.kotlin-result:kotlin-result-coroutines:_")

    implementation(AndroidX.test.coreKtx)

    implementation(AndroidX.test.runner)
    implementation(AndroidX.test.ext.junit)
    implementation(AndroidX.test.ext.truth)

    implementation(Square.retrofit2.retrofit)

    implementation(KotlinX.coroutines.test)
}

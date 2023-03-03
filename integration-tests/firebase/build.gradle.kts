import com.android.build.api.dsl.ManagedVirtualDevice
import com.omricat.gradle.BuildVersions
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies { classpath(Square.okHttp3) }
}

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

    buildTypes.named("debug") { isDebuggable = true }

    sourceSets.forEach { srcSet -> srcSet.java.srcDir("src/${srcSet.name}/kotlin") }

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

fun failBuildIfFirebaseEmulatorIsNotRunning() {
    logger.lifecycle("Checking whether Firebase emulator is reachable")
    val client = OkHttpClient()
    val baseUrl = HttpUrl.Builder().scheme("http").host("localhost").build()

    listOf(Ports.AUTH, Ports.FIRESTORE).forEach { port ->
        val request = Request.Builder().url(baseUrl.newBuilder().port(port).build()).build()
        try {
            client.newCall(request).execute().use { resp ->
                check(resp.isSuccessful) { "Can't connect to Firebase emulator at ${request.url}" }
            }
        } catch (e: okio.IOException) {
            throw IllegalStateException("Can't connect to Firebase emulator at ${request.url}", e)
        }
    }
    logger.lifecycle("Firebase emulator found!")
}

object Ports {
    const val FIRESTORE = 8080
    const val AUTH = 9099
}

gradle.taskGraph.whenReady {
    if (
        allTasks.any {
            it.project == project &&
                it.name.contains("AndroidTest", ignoreCase = true) &&
                it.name.contains("assemble", ignoreCase = true).not()
        }
    ) {
                failBuildIfFirebaseEmulatorIsNotRunning()
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

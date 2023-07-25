@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.ManagedVirtualDevice
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies { classpath(libs.okhttp) }
}

plugins {
    alias(libs.plugins.maplib.android.test)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.omricat.maplibrarian.integrationtesting.debug"
    targetProjectPath = ":app"

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
    debugImplementation(projects.core)
    debugImplementation(projects.app)

    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.test)

    implementation(platform(libs.kotlinx.serialization.bom))
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestoreKtx)
    implementation(libs.firebase.authKtx)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinXSerialization)

    implementation(projects.util.kotlinResultAssertkExtensions)
    implementation(libs.kotlinResult)
    implementation(libs.kotlinResult.coroutines)

    implementation(androidx.test.coreKtx)

    implementation(androidx.test.runner)
    implementation(androidx.test.ext.junitKtx)

    implementation(libs.assertk)
}

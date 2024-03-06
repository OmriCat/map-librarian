package com.omricat.maplibrarian.gradle

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

internal fun Project.configureAndroidKotlin(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    val javaVersion = javaVersionFromGradleProperties().get()
    with(commonExtension) {
        compileSdk = compileSdkFromGradleProperties().get()

        defaultConfig {
            minSdk = minSdkFromGradleProperties().get()
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }

        sourceSets.configureEach { srcSet -> srcSet.java.srcDir("src/${srcSet.name}/kotlin") }
    }
}

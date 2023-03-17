package com.omricat.maplibrarian.build

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
internal fun Project.configureAndroidKotlin(commonExtension: CommonExtension<*, *, *, *>) {
    val javaVersion = javaVersionFromGradleProperties().get()
    with(commonExtension) {
        compileSdk = compileSdkFromGradleProperties().get()

        defaultConfig.minSdk = minSdkFromGradleProperties().get()

        compileOptions {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }

        sourceSets.configureEach { srcSet -> srcSet.java.srcDir("src/${srcSet.name}/kotlin") }

        (this as ExtensionAware).extensions.configure<KotlinJvmOptions> {
            jvmTarget = javaVersion.toString()
        }
    }
}

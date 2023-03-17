package com.omricat.maplibrarian.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLanguageVersion

internal fun Project.javaLanguageVersionFromGradleProperties(): Provider<JavaLanguageVersion> =
    javaVersionFromGradleProperties().map { s -> JavaLanguageVersion.of(s.majorVersion) }

internal fun Project.javaVersionFromGradleProperties(): Provider<JavaVersion> =
    gradleProperty("javaversion").map { JavaVersion.toVersion(it) }.orElse(JavaVersion.VERSION_11)

private const val DEFAULT_COMPILE_SDK = 33

internal fun Project.compileSdkFromGradleProperties(): Provider<Int> =
    gradleProperty("compilesdk").orElse(DEFAULT_COMPILE_SDK)

private const val DEFAULT_MIN_SDK = 23

internal fun Project.minSdkFromGradleProperties(): Provider<Int> =
    gradleProperty("minSdk").orElse(DEFAULT_MIN_SDK)

private const val DEFAULT_TARGET_SDK = 29

internal fun Project.targetSdkFromGradleProperties(): Provider<Int> =
    gradleProperty("targetSdk").orElse(DEFAULT_TARGET_SDK)

private fun Project.gradleProperty(id: String): Provider<Int> =
    providers.gradleProperty("com.omricat.maplib.${id}").map { s -> s.toInt() }

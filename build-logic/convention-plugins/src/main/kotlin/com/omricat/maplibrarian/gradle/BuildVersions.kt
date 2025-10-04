package com.omricat.maplibrarian.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.getByType

internal fun Project.javaLanguageVersionFromGradleProperties(): Provider<JavaLanguageVersion> =
    javaVersionFromGradleProperties().map { s -> JavaLanguageVersion.of(s.majorVersion) }

internal fun Project.javaVersionFromGradleProperties(): Provider<JavaVersion> =
    versionFromCatalog("javaVersion").map { JavaVersion.toVersion(it) }

private fun Project.versionFromCatalog(identifier: String): Provider<Int> =
    providers
        .provider {
            extensions
                .getByType<VersionCatalogsExtension>()
                .named("libs")
                .findVersion(identifier)
                .get()
                .requiredVersion
        }
        .map { str -> str.toInt() }

internal fun Project.compileSdkFromGradleProperties(): Provider<Int> =
    versionFromCatalog("compileSdk")

internal fun Project.minSdkFromGradleProperties(): Provider<Int> = versionFromCatalog("minSdk")

private const val DEFAULT_TARGET_SDK = 29

internal fun Project.targetSdkFromGradleProperties(): Provider<Int> =
    versionFromCatalog("targetSdk").orElse(DEFAULT_TARGET_SDK)

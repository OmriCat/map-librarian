package com.omricat.maplibrarian.gradle

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension

internal fun Project.configureJavaToolchainForKotlin(kotlinExtension: KotlinBaseExtension) =
    with(kotlinExtension) {
        jvmToolchain { it.languageVersion.set(javaLanguageVersionFromGradleProperties()) }
    }

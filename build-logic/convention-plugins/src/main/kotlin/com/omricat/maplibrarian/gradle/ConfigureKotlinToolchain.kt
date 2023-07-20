package com.omricat.maplibrarian.gradle

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

internal fun Project.configureJavaToolchainForKotlin(kotlinExtension: KotlinTopLevelExtension) =
    with(kotlinExtension) {
        jvmToolchain { it.languageVersion.set(javaLanguageVersionFromGradleProperties()) }
    }

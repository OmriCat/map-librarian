@file:Suppress("SpellCheckingInspection")

import de.fayard.refreshVersions.core.versionFor

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(Google.playServicesGradlePlugin)
    }
}

plugins {
    id("com.android.application") apply false
    kotlin("android") apply false
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.dorongold.task-tree")
    id("com.autonomousapps.dependency-analysis")
    idea
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("io.gitlab.arturbosch.detekt")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("org.gradle.idea")
    }

    ktlint {
        debug.set(false)
        version.set(versionFor("com.pinterest:ktlint:_"))
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(true)
        enableExperimentalRules.set(true)
        disabledRules.addAll("experimental:argument-list-wrapping")
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }

    detekt {
        config = rootProject.files("config/detekt/detekt.yml")
        reports {
            html {
                enabled = true
                destination = file("build/reports/detekt.html")
            }
        }
    }

    idea.module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
            )
            languageVersion = "1.5"
        }
    }
    afterEvaluate {
        val hasKotlin = plugins.any {
            it is org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
        }
        if (hasKotlin) {
            extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension>().apply {
                jvmToolchain {
                    (this as JavaToolchainSpec).languageVersion.set(
                        JavaLanguageVersion.of(buildVersions.javaLanguageVersion)
                    )
                }
            }
        }

        val hasAndroidPlugin = plugins.any {
            it is com.android.build.gradle.api.AndroidBasePlugin
        }
        if (hasAndroidPlugin) {
            extensions.getByType<com.android.build.gradle.BaseExtension>().apply {
                compileOptions {
                    sourceCompatibility = JavaVersion.toVersion(buildVersions.javaLanguageVersion)
                    targetCompatibility = JavaVersion.toVersion(buildVersions.javaLanguageVersion)
                }
            }
        }
    }
}

idea.module {
    isDownloadJavadoc = true
    isDownloadSources = true
}

@Suppress("MagicNumber")
val buildVersions by extra(
    com.omricat.gradle.BuildVersions(
        compileSdk = 31,
        minSdk = 21,
        targetSdk = 29,
        javaLanguageVersion = 11,
    )
)

@file:Suppress("SpellCheckingInspection")

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
    id("com.dorongold.task-tree")
    id("com.autonomousapps.dependency-analysis")
    id("com.osacky.doctor")
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
        plugin("org.gradle.idea")
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

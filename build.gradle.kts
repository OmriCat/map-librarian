@file:Suppress("SpellCheckingInspection")

import com.ncorti.ktfmt.gradle.tasks.KtfmtBaseTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtCheckTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask

plugins {
    id("com.android.application") apply false
    id("com.android.library") apply false
    kotlin("android") apply false
    id("com.google.gms.google-services") apply false
    id("com.ncorti.ktfmt.gradle")
    id("io.gitlab.arturbosch.detekt")
    id("com.dorongold.task-tree")
    id("com.autonomousapps.dependency-analysis")
    id("com.osacky.doctor")
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    apply { plugin("com.ncorti.ktfmt.gradle") }

    ktfmt { kotlinLangStyle() }
}

subprojects {
    apply { plugin("io.gitlab.arturbosch.detekt") }

    detekt {
        config = rootProject.files("config/detekt/detekt.yml")
        reports {
            html {
                enabled = true
                destination = file("build/reports/detekt.html")
            }
        }
    }

    afterEvaluate {
        val hasKotlin =
            plugins.any { it is org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper }
        if (hasKotlin) {
            extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension>().apply {
                jvmToolchain {
                    (this as JavaToolchainSpec)
                        .languageVersion
                        .set(JavaLanguageVersion.of(buildVersions.javaLanguageVersion))
                }
            }
        }

        val hasAndroidPlugin = plugins.any { it is com.android.build.gradle.api.AndroidBasePlugin }
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

@Suppress("MagicNumber")
val buildVersions by
    extra(
        com.omricat.gradle.BuildVersions(
            compileSdk = 33,
            minSdk = 23,
            targetSdk = 29,
            javaLanguageVersion = 11,
        )
    )

// Check task below used for CI, and format task to easily format all kotlin source & script files
fun KtfmtBaseTask.configureForAllKtsAndKt() {
    source = fileTree(rootDir)
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/tools/**", "**/scripts/**")
    notCompatibleWithConfigurationCache("Not known why task isn't compatible with config cache")
}

tasks.register<KtfmtCheckTask>("ktfmtCheckAllKtsAndKt") { configureForAllKtsAndKt() }

tasks.register<KtfmtFormatTask>("ktfmtFormatAllKtsAndKt") { configureForAllKtsAndKt() }

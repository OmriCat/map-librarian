@file:Suppress("SpellCheckingInspection")

plugins {
    // All plugins used within the build to ensure versions are aligned throughout
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.googleServices) apply false

    // Convention plugins used within the build
    alias(libs.plugins.maplib.kotlin.library) apply false

    // Convention plugin for the root project
    alias(libs.plugins.maplib.root)

    // Additional root project plugins
    alias(libs.plugins.taskTree)
    alias(libs.plugins.dependencyAnalysis)
    alias(libs.plugins.gradleDoctor)
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
                    (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
                }
            }
        }

        val hasAndroidPlugin = plugins.any { it is com.android.build.gradle.api.AndroidBasePlugin }
        if (hasAndroidPlugin) {
            extensions.getByType<com.android.build.gradle.BaseExtension>().apply {
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }
            }
        }
    }
}

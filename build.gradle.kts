@file:Suppress("SpellCheckingInspection")

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
}

subprojects {
    apply {
        plugin("com.ncorti.ktfmt.gradle")
        plugin("io.gitlab.arturbosch.detekt")
        plugin("org.gradle.idea")
    }

    ktfmt { kotlinLangStyle() }

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

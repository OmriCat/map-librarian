@file:Suppress("SpellCheckingInspection")

import com.ncorti.ktfmt.gradle.tasks.KtfmtBaseTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtCheckTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.taskTree)
    alias(libs.plugins.dependencyAnalysis)
    alias(libs.plugins.gradleDoctor)
    id("com.omricat.maplib.root")
}

buildVersions {
    javaLanguageVersion = 11
    compileSdk = 33
    minSdk = 23
    targetSdk = 29
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

// @Suppress("MagicNumber")
// val buildVersions by
//    extra(
//        com.omricat.gradle.BuildVersions(
//            compileSdk = 33,
//            minSdk = 23,
//            targetSdk = 29,
//            javaLanguageVersion = 11,
//        )
//    )

// Check task below used for CI, and format task to easily format all kotlin source & script files
fun KtfmtBaseTask.configureForAllKtsAndKt() {
    source = fileTree(rootDir)
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/tools/**", "**/scripts/**")
    notCompatibleWithConfigurationCache("Not known why task isn't compatible with config cache")
}

tasks.register<KtfmtCheckTask>("ktfmtCheckAllKtsAndKt") { configureForAllKtsAndKt() }

tasks.register<KtfmtFormatTask>("ktfmtFormatAllKtsAndKt") { configureForAllKtsAndKt() }

// Task for running detekt on all files, including *.kts

val detektAll: Configuration by configurations.creating

tasks.register<JavaExec>("detektAll") {
    mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")
    classpath = detektAll
    val input = rootDir.absolutePath
    val config = rootDir.resolve("config").resolve("detekt").resolve("detekt.yml").absolutePath
    val excludes =
        listOf("build", "resources", "tools", "scripts").joinToString(separator = ",") {
            "**/$it/*"
        }
    val params = listOf("--input", input, "--config", config, "--excludes", excludes)
    logger.lifecycle("Running detekt cli tool with parameters: $params")
    args = params
}

dependencies {
    detektAll("io.gitlab.arturbosch.detekt:detekt-cli") {
        version { strictly(libs.versions.plugin.detekt.get()) }
    }
}

@file:Suppress("SpellCheckingInspection")

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.google.gms:google-services:_")
    }
}

plugins {
    id("com.android.application") apply false
    kotlin("android") apply false
//    id("com.google.gms.google-services").apply(false).version("4.3.3")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("dev.ahmedmourad.nocopy.nocopy-gradle-plugin").apply(false)
    idea
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter() // TODO: Remove jcenter repo before 1/5/2021
    }
}

subprojects {
    apply {
        plugin("io.gitlab.arturbosch.detekt")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("dev.ahmedmourad.nocopy.nocopy-gradle-plugin")
    }

    ktlint {
        debug.set(false)
        version.set("0.37.2")
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        enableExperimentalRules.set(true)
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

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
            languageVersion = "1.5"
        }
    }
}

idea.module {
    isDownloadJavadoc = true
    isDownloadSources = true
}

@Suppress("MagicNumber")
val androidBuildVersions by extra(object : com.omricat.gradle.AndroidBuildToolVersions {
    override val compileSdk: Int = 30
    override val minSdk: Int = 21
    override val targetSdk: Int = 29
})

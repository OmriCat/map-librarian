@file:Suppress("SpellCheckingInspection")

plugins {
    id("com.android.application") apply false
    kotlin("android") apply false
    id("com.google.gms.google-services").apply(false).version("4.3.3")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("dev.ahmedmourad.nocopy.nocopy-gradle-plugin").apply(false).version("1.1.0")
    idea
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://dl.bintray.com/arkivanov/maven")
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

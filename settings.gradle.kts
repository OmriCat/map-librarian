@file:Suppress("SpellCheckingInspection")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        val androidx by creating { from(files("gradle/androidx.versions.toml")) }
    }
}

plugins {
    id("com.gradle.enterprise") version "3.12.2"
    id("de.fayard.refreshVersions") version "0.51.0"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.5"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

refreshVersions {
    rejectVersionIf { candidate.stabilityLevel.isLessStableThan(current.stabilityLevel) }
}

gitHooks {
    commitMsg { conventionalCommits() }
    createHooks()
}

rootProject.name = ("map-librarian")

include(
    ":app",
    ":core",
    ":kotlin-result-kotest",
    ":integration-tests:firebase",
)

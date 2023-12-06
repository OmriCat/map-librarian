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
    id("com.gradle.enterprise") version "3.16"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.16"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

gitHooks {
    commitMsg { conventionalCommits() }
    createHooks()
}

rootProject.name = ("map-librarian")

include(
    ":app",
    ":core",
    ":util:kotlin-result-extensions",
    ":util:logging",
    ":util:logging:test",
    ":integration-tests:firebase",
)

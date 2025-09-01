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
    id("com.gradle.develocity") version "4.1.1"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.1.0"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
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

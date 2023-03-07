@file:Suppress("SpellCheckingInspection")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    resolutionStrategy {
        val pluginIdToCoordinates =
            mapOf("com.google.gms.google-services" to "com.google.gms:google-services")
        eachPlugin {
            pluginIdToCoordinates[requested.id.id]?.also { useModule("$it:${requested.version}") }
        }
    }
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
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
    preCommit {
        from(rootDir.resolve("scripts").resolve("check-formatting"))
    }
    commitMsg {
        conventionalCommits()
    }
    createHooks()
}

rootProject.name = ("map-librarian")

include(
    ":app",
    ":core",
    ":kotlin-result-kotest",
    ":firebase-emulator-container",
    ":firebase:auth",
    //TODO: Create firebase:auth subproject to hold firebase auth integration code/tests
    // see https://github.com/grodin/map-librarian/issues/82
)

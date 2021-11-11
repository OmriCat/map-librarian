@file:Suppress("SpellCheckingInspection")

pluginManagement {
    resolutionStrategy {
        val pluginIdToCoordinates = mapOf(
            "com.google.gms.google-services" to "com.google.gms:google-services"
        )
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
    id("de.fayard.refreshVersions") version "0.23.0"
}

refreshVersions {
    rejectVersionIf {
        candidate.stabilityLevel.isLessStableThan(current.stabilityLevel)
    }
}
rootProject.name = ("map-librarian")

include(
    ":app",
    ":core",
)

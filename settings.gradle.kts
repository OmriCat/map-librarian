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
    id("de.fayard.refreshVersions") version "0.11.0"
////                            # available:"0.20.0"
////                            # available:"0.21.0"
}

rootProject.name = ("map-librarian")

include(
    "app"
)

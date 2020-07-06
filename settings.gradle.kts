pluginManagement {
    resolutionStrategy {
        val pluginIdToCoordinates = mapOf(
            "com.android.library" to "com.android.tools.build:gradle",
            "com.android.application" to "com.android.tools.build:gradle",
            "com.google.gms.google-services" to "com.google.gms:google-services:4.3.3"
        )
        eachPlugin {
            pluginIdToCoordinates[requested.id.id]?.also { useModule("$it:${requested.version}") }
        }
    }
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        jcenter()
    }
}

rootProject.name = ("kotlin-android-template")

include(
    "app",
    "ui",
    "domain"
)

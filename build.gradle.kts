@file:Suppress("SpellCheckingInspection")

plugins {
    // All plugins used within the build to ensure versions are aligned throughout
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.googleServices) apply false

    // Convention plugins used within the build
    alias(libs.plugins.maplib.kotlin.library) apply false
    alias(libs.plugins.maplib.android.application) apply false
    alias(libs.plugins.maplib.android.test) apply false

    // Convention plugin for the root project
    alias(libs.plugins.maplib.root)

    // Additional root project plugins
    alias(libs.plugins.taskTree)
    alias(libs.plugins.dependencyAnalysis)
    alias(libs.plugins.gradleDoctor)
    alias(libs.plugins.moduleGraphAssertion)
}

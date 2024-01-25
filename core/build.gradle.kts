plugins {
    alias(libs.plugins.maplib.kotlin.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xcontext-receivers") } }

dependencies {
    implementation(projects.util.logging)

    implementation(platform(libs.kotlinx.coroutines.bom))
    api(libs.kotlinx.coroutines.core)

    api(libs.workflow.core.jvm)

    api(libs.kotlinResult)

    api(platform(libs.kotlinx.serialization.bom))
    api(libs.kotlinx.serialization.json)

    testImplementation(projects.util.kotlinResultExtensions)
    testImplementation(libs.assertk)

    testImplementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.workflow.testing.jvm)
}

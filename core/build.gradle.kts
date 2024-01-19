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

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)

    testImplementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.workflow.testing.jvm)
}

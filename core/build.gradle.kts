plugins {
    alias(libs.plugins.maplib.kotlin.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(KotlinX.coroutines.core)

    fun workflow(artifact: String) = "com.squareup.workflow1:workflow-$artifact:_"
    api(workflow("core-jvm"))

    api("com.michael-bull.kotlin-result:kotlin-result:_")

    api(KotlinX.serialization.json)

    testImplementation(Testing.kotest.runner.junit5)
    testImplementation(Testing.kotest.assertions.core)

    testImplementation(KotlinX.coroutines.test)

    testImplementation(workflow("testing-jvm"))
}

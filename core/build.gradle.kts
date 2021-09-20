plugins {
    `java-library`
    kotlin("jvm")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(KotlinX.coroutines.core)

    fun workflow(artifact: String) = "com.squareup.workflow1:workflow-$artifact:_"
    implementation(workflow("core-jvm"))

    implementation("com.michael-bull.kotlin-result:kotlin-result:_")

    testImplementation(Testing.kotest.runner.junit5)
    testImplementation(Testing.kotest.assertions.core)
    testImplementation(Testing.kotest.core)

    testImplementation(KotlinX.coroutines.test)

    testImplementation(workflow("testing-jvm"))
}

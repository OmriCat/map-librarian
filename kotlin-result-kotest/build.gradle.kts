plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api("com.michael-bull.kotlin-result:kotlin-result:_")
    api(Testing.kotest.assertions.core)
}

kotlin { sourceSets.all { languageSettings.optIn("kotlin.contracts.ExperimentalContracts") } }

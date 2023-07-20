plugins { alias(libs.plugins.maplib.kotlin.library) }

dependencies {
    api(libs.kotlinResult)
    api(libs.kotest.assertions.core)
}

kotlin { sourceSets.all { languageSettings.optIn("kotlin.contracts.ExperimentalContracts") } }

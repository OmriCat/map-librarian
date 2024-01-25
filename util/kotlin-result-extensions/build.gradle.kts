plugins { alias(libs.plugins.maplib.kotlin.library) }

dependencies {
    api(libs.kotlinResult)
    api(libs.assertk)
}

kotlin { sourceSets.all { languageSettings.optIn("kotlin.contracts.ExperimentalContracts") } }

testing { suites { withType<JvmTestSuite>().configureEach { useKotlinTest() } } }

plugins {
    id("com.android.application") version "4.1.0" apply false
    kotlin("android") version "1.4.10" apply false
    id("com.google.gms.google-services") version "4.3.3" apply false
    id("io.gitlab.arturbosch.detekt") version "1.9.1"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    idea
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

subprojects {
    apply {
        plugin("io.gitlab.arturbosch.detekt")
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    ktlint {
        debug.set(false)
        version.set("0.37.2")
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        enableExperimentalRules.set(true)
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }

    detekt {
        config = rootProject.files("config/detekt/detekt.yml")
        reports {
            html {
                enabled = true
                destination = file("build/reports/detekt.html")
            }
        }
    }
}

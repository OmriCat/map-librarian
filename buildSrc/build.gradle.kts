import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    `kotlin-dsl`
    `java-gradle-plugin`
}
repositories {
    jcenter()
    mavenCentral()
    google()
}

// Shared build configuration which is required both in buildSrc and in the main build
// TODO: Nice way to share config details between buildSrc and main build

object SharedBuildConfig {
    const val agp = "4.0.1"

}

gradlePlugin {
    plugins {
        create("MapLibrarianPlugin") {
            id = "map-librarian"
            implementationClass = "com.omricat.maplibrarian.gradle.MapLibrarianPlugin"
        }
    }
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("gradle-plugin"))

    implementation("com.android.tools.build:gradle:${SharedBuildConfig.agp}")
    implementation("com.android.tools.build:gradle-api:${SharedBuildConfig.agp}")
    implementation(kotlin("stdlib-jdk8"))
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

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
    const val agp = "4.0.0"

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
    implementation(kotlin("gradle-plugin"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk7"))
    implementation(kotlin("stdlib-common"))
    implementation(kotlin("reflect"))

    implementation("com.android.tools.build:gradle:${SharedBuildConfig.agp}")

}

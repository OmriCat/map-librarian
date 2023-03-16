plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(platform("org.testcontainers:testcontainers-bom:_"))
    api("org.testcontainers:testcontainers")
}

tasks.processResources { from(rootProject.layout.projectDirectory.file("firestore.rules")) }

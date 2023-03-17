plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
}

group = "com.omricat.map-librarian"

version = libs.versions.plugin.convention.maplib.get()

kotlin { explicitApi() }

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(libs.ktfmt.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.detekt.cli)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.android.gradlePlugin.api)
}

val Provider<PluginDependency>.id
    get() = this.get().pluginId

val conventionPlugins
    get() = project.libs.plugins.maplib

gradlePlugin {
    plugins {
        register("root") {
            id = conventionPlugins.root.id
            implementationClass = "RootProjectPlugin"
        }
        register("androidApplication") {
            id = conventionPlugins.android.application.id
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("kotlinLibrary") {
            id = conventionPlugins.kotlin.library.id
            implementationClass = "KotlinLibraryConventionPlugin"
        }
    }
}

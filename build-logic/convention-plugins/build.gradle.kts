plugins { `kotlin-dsl` }

group = "com.omricat.map-librarian"

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.ktfmt.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.detekt.cli)
}

val Provider<PluginDependency>.id
    get() = this.get().pluginId

val conventionPlugins
    get() = project.libs.plugins.maplib

gradlePlugin {
    plugins {
        register("sharedBuildVersions") {
            id = conventionPlugins.root.id
            implementationClass = "RootProjectPlugin"
        }
    }
}

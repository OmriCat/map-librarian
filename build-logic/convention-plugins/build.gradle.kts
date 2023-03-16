plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("sharedBuildVersions") {
            id = "com.omricat.maplib.root"
            implementationClass = "RootProjectPlugin"
        }

    }
}

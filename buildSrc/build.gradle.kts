plugins {
    `kotlin-dsl`
    idea
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:_")
}

extensions.findByType<org.gradle.plugins.ide.idea.model.IdeaModel>()?.module {
    isDownloadSources = true
    isDownloadJavadoc = true
}

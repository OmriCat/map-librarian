plugins {
    `kotlin-dsl`
    idea
}

repositories {
    google()
    mavenCentral()
}

extensions.findByType<org.gradle.plugins.ide.idea.model.IdeaModel>()?.module {
    isDownloadSources = true
    isDownloadJavadoc = true
}

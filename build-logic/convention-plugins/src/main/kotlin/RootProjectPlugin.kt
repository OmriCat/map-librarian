import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

@Suppress("unused")
class RootProjectPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.extensions.create("buildVersions", BuildVersionsExtension::class)
    }
}


interface BuildVersionsExtension {
    var compileSdk: Int
    var minSdk: Int
    var targetSdk: Int
    var javaLanguageVersion: Int
}

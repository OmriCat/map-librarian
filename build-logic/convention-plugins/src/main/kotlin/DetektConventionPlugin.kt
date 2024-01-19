import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import java.io.File

internal class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(DetektPlugin::class)
            configure<DetektExtension> {
                config.setFrom(rootProject.files(DETEKT_CONFIG_FILE))
            }
            tasks.withType<Detekt>().configureEach { task ->
                task.reports.html {
                    it.required.set(true)
                    it.outputLocation.set(rootProject.layout.buildDirectory.file("reports/detekt.html"))
                }
            }
        }
    }

    companion object {
        const val DETEKT_CONFIG_FILE = "config/detekt/detekt.yml"
    }
}

import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(DetektPlugin::class)
            configure<DetektExtension> {
                config = rootProject.files(DETEKT_CONFIG_FILE)
                reports {
                    html {
                        enabled = true
                        destination = file("build/reports/detekt.html")
                    }
                }
            }
        }
    }

    companion object {
        const val DETEKT_CONFIG_FILE = "config/detekt/detekt.yml"
    }
}

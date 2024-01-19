import com.ncorti.ktfmt.gradle.tasks.KtfmtBaseTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtCheckTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask
import com.omricat.maplibrarian.gradle.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin

@Suppress("unused")
public class RootProjectPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(KtfmtConventionPlugin::class)

            tasks.register<KtfmtCheckTask>("ktfmtCheckAllKtsAndKt") {
                description = "Checks formatting of all Kotlin source and Kotlin script files"
                configureForAllKtsAndKt(target)
            }

            tasks.register<KtfmtFormatTask>("ktfmtFormatAllKtsAndKt") {
                description = "Formats all Kotlin source and Kotlin script files"
                configureForAllKtsAndKt(target)
            }

            pluginManager.apply(DetektConventionPlugin::class)

            // Task for running detekt on all files, including *.kts

            val detektAll: Configuration = configurations.maybeCreate("detektAll")

            tasks.register<JavaExec>(detektAll.name) {
                description = "Run detekt on all Kotlin source and Kotlin script files"
                group = LifecycleBasePlugin.VERIFICATION_GROUP
                mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")
                classpath = detektAll
                val input = rootDir.absolutePath
                val config = DetektConventionPlugin.DETEKT_CONFIG_FILE
                val excludes: String =
                    listOf("build", "resources", "tools", "scripts").joinToString(separator = ",") {
                        "**/$it/*"
                    }
                val reports = "txt:${rootProject.layout.buildDirectory.get()}/reports/detektAll.txt"
                val params =
                    listOf(
                        "--input",
                        input,
                        "--config",
                        config,
                        "--excludes",
                        excludes,
                        "--report",
                        reports,
                        "--debug"
                    )
                args = params
                doFirst { logger.lifecycle("Running detekt cli tool with parameters: $params") }
            }

            dependencies { detektAll(libs.findLibrary("detekt_cli").get()) }
        }
    }
}

private fun KtfmtBaseTask.configureForAllKtsAndKt(project: Project) {
    source = project.fileTree(project.rootDir)
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/tools/**", "**/scripts/**")
    notCompatibleWithConfigurationCache("Not known why task isn't compatible with config cache")
}

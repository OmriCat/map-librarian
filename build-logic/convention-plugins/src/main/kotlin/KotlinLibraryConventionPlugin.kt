import com.omricat.maplibrarian.gradle.javaLanguageVersionFromGradleProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

@Suppress("unused")
public class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(JavaLibraryPlugin::class)

            tasks.withType<Test>().configureEach { it.useJUnitPlatform() }

            pluginManager.apply("org.jetbrains.kotlin.jvm")

            configure<KotlinJvmProjectExtension> {
                explicitApi()
                jvmToolchain { it.languageVersion.set(javaLanguageVersionFromGradleProperties()) }
            }

            pluginManager.apply(KtfmtConventionPlugin::class)

            pluginManager.apply(DetektConventionPlugin::class)
        }
    }
}

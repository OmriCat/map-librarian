import com.omricat.maplibrarian.gradle.configureJavaToolchainForKotlin
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
            with(pluginManager) {
                apply(JavaLibraryPlugin::class)
                apply("org.jetbrains.kotlin.jvm")
                apply(KtfmtConventionPlugin::class)
                apply(DetektConventionPlugin::class)
            }

            tasks.withType<Test>().configureEach { it.useJUnitPlatform() }

            configure<KotlinJvmProjectExtension> {
                explicitApi()
                configureJavaToolchainForKotlin(this)
            }
        }
    }
}

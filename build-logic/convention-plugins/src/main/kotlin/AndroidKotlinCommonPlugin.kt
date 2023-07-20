import com.omricat.maplibrarian.gradle.configureJavaToolchainForKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

public class AndroidKotlinCommonPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.android")
                apply(KtfmtConventionPlugin::class)
                apply(DetektConventionPlugin::class)
            }

            configure<KotlinAndroidProjectExtension> { configureJavaToolchainForKotlin(this) }
        }
    }
}

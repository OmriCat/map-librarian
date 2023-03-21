import com.android.build.api.dsl.ApplicationExtension
import com.omricat.maplibrarian.gradle.configureAndroidKotlin
import com.omricat.maplibrarian.gradle.targetSdkFromGradleProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

public class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply(KtfmtConventionPlugin::class)
                apply(DetektConventionPlugin::class)
            }

            extensions.configure<ApplicationExtension> {
                configureAndroidKotlin(this)

                defaultConfig.targetSdk = targetSdkFromGradleProperties().get()

                buildTypes {
                    named("debug").get().apply {
                        isMinifyEnabled = false
                        isDebuggable = true
                        applicationIdSuffix = ".debug"
                        versionNameSuffix = "-debug"
                    }
                }

                dataBinding.enable = false
            }
        }
    }
}

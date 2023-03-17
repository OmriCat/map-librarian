import com.android.build.api.dsl.ApplicationExtension
import com.omricat.maplibrarian.build.configureAndroidKotlin
import com.omricat.maplibrarian.build.targetSdkFromGradleProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

public class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.android")

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
            }
        }
    }
}

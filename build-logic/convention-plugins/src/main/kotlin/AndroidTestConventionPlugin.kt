import com.android.build.api.dsl.TestExtension
import com.omricat.maplibrarian.gradle.configureAndroidKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

public class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.test")
                apply(AndroidKotlinCommonPlugin::class)
            }
            configure<TestExtension> { configureAndroidKotlin(this) }
        }
    }
}

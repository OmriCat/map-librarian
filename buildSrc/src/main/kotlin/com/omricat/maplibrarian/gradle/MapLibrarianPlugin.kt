package com.omricat.maplibrarian.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper

@Suppress("unused")
class MapLibrarianPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            configureAndroid()
            configureKotlin()
        }
    }

    private fun Project.configureKotlin() {
        // NOOP
    }

    private fun Project.configureAndroid() {
        whenEvaluated {
            if (hasKotlinPlugin()) {
                extensions.findByType<BaseExtension>()?.apply {
                    sourceSets.forEach { srcSet -> srcSet.java.srcDir("src/${srcSet.name}/kotlin") }
                }
            }
        }

    }
}

private fun <T> Project.whenEvaluated(fn: Project.() -> T) {
    if (state.executed)
        fn()
    else
        afterEvaluate { fn() }
}

private fun Project.hasKotlinPlugin(): Boolean =
    plugins.asSequence().mapNotNull { it as? KotlinBasePluginWrapper }.firstOrNull() != null


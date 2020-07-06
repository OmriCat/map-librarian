package com.omricat.maplibrarian.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper

@Suppress("unused")
class MapLibrarianPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        if (hasKotlinPlugin())
            configureAndroid()
            configureKotlin()
        }

    private fun Project.configureKotlin() {
        // NOOP
    }

    private fun Project.configureAndroid() {
        val sourceSetsToConfigure = listOf("main", "test", "debug", "release")
        extensions.findByType<BaseExtension>()?.apply {
            sourceSetsToConfigure.forEach { srcSet ->
                sourceSets[srcSet].java.srcDir("/src/$srcSet/kotlin")
            }
        }
    }
}

private fun Project.hasKotlinPlugin(): Boolean =
    plugins.asSequence().mapNotNull { it as? KotlinBasePluginWrapper }.firstOrNull() != null

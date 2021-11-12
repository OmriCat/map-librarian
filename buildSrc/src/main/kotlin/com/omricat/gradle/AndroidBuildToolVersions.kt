package com.omricat.gradle

interface AndroidBuildToolVersions {
    val compileSdk: Int
    val minSdk: Int
    val targetSdk: Int

    companion object {
        operator fun invoke(
            compileSdk: Int,
            minSdk: Int,
            targetSdk: Int
        ): AndroidBuildToolVersions = object : AndroidBuildToolVersions {
            override val compileSdk: Int = compileSdk
            override val minSdk: Int = minSdk
            override val targetSdk: Int = targetSdk
        }
    }
}

package com.omricat.gradle

interface BuildVersions {
    val compileSdk: Int
    val minSdk: Int
    val targetSdk: Int
    val javaLanguageVersion: Int

    companion object {
        operator fun invoke(
            compileSdk: Int,
            minSdk: Int,
            targetSdk: Int,
            javaLanguageVersion: Int
        ): BuildVersions = object : BuildVersions {
            override val compileSdk: Int = compileSdk
            override val minSdk: Int = minSdk
            override val targetSdk: Int = targetSdk
            override val javaLanguageVersion: Int = javaLanguageVersion
        }
    }
}

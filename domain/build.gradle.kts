
plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk7"))

    testImplementation(TestingLib.JUNIT)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}


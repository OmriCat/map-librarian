package com.omricat.firebaseemulatorcontainer

import org.testcontainers.containers.BindMode.READ_ONLY
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

class FirebaseEmulatorContainer : GenericContainer<FirebaseEmulatorContainer>(
    DockerImageName.parse("ghcr.io/grodin/firebase-emulator-docker:v$DOCKER_IMAGE_VERSION")
) {
    init {
        withExposedPorts(FIRESTORE_PORT, AUTH_PORT)
        waitingFor(Wait.forHealthcheck())
        withClasspathResourceMapping("firestore.rules", "./firestore.rules", READ_ONLY)
        withCommand("emulators:start")
    }

    companion object {
        const val DOCKER_IMAGE_VERSION = "1.2.0"
        const val FIRESTORE_PORT = 8080
        const val AUTH_PORT = 9099
    }
}

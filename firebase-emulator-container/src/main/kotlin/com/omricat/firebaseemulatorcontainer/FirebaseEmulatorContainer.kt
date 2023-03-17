package com.omricat.firebaseemulatorcontainer

import org.testcontainers.containers.BindMode.READ_ONLY
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

public class FirebaseEmulatorContainer :
    GenericContainer<FirebaseEmulatorContainer>(
        DockerImageName.parse("ghcr.io/grodin/firebase-emulator-docker:v$DOCKER_IMAGE_VERSION")
    ) {
    init {
        withClasspathResourceMapping("firestore.rules", "./firestore.rules", READ_ONLY)
        withExposedPorts(FIRESTORE_PORT, AUTH_PORT)
        waitingFor(Wait.forHealthcheck())
        withCommand("emulators:start", "--project", "map-librarian")
    }

    public companion object {
        public const val DOCKER_IMAGE_VERSION: String = "1.4.1"
        public const val FIRESTORE_PORT: Int = 8080
        public const val AUTH_PORT: Int = 9099
    }
}

package com.omricat.maplibrarian

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.omricat.maplibrarian.auth.ActualAuthWorkflow
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.auth.FirebaseAuthService
import com.omricat.maplibrarian.userdetails.UserDetailsWorkFlow

internal fun MapLibraryApp.initializeDI(): MapLibDiContainer = object : MapLibDiContainer {
    override val authService: AuthService by lazy {
        FirebaseAuth.getInstance().useEmulator("192.168.1.17", 9099)
        FirebaseAuthService(Firebase.auth)
    }

    override val workflows: MapLibDiContainer.Workflows = object : MapLibDiContainer.Workflows {
        override val auth: AuthWorkflow by lazy { ActualAuthWorkflow(authService) }

        override val userDetails: UserDetailsWorkFlow by lazy { UserDetailsWorkFlow }
    }
}

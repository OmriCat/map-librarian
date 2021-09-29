package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.toResultOr
import com.google.firebase.auth.FirebaseAuth
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.UserUid
import com.omricat.maplibrarian.utils.runSuspendCatching
import kotlinx.coroutines.tasks.await

internal class FirebaseAuthService(private val auth: FirebaseAuth) : AuthService {
    override suspend fun getSignedInUserIfAny(): Result<User?, AuthError> =
        runSuspendCatching { auth.currentUser }
            .mapError(::AuthError)
            .map { user -> user?.let { FirebaseUser(it) } }

    override suspend fun attemptAuthentication(credential: Credential): Result<User, AuthError> =
        when (credential) {
            is EmailPasswordCredential ->
                runSuspendCatching {
                    auth.signInWithEmailAndPassword(credential.emailAddress, credential.password)
                        .await()
                        .user
                }.mapError(::AuthError)
                    .andThen { user -> // If user is null, no user is signed in
                        user.toResultOr { AuthError("No currently signed in user") }
                            .map { FirebaseUser(it) }
                    }
        }

    override fun signOut() = auth.signOut()
}

@JvmInline
internal value class FirebaseUser(private val user: com.google.firebase.auth.FirebaseUser) : User {
    override val displayName: String
        get() = user.displayName ?: "(unknown name)"

    override val id: UserUid
        get() = UserUid(user.uid)
}

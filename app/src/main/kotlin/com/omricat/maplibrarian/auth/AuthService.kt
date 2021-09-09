package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import com.google.firebase.auth.FirebaseAuth
import com.omricat.maplibrarian.User
import kotlinx.coroutines.tasks.await

interface AuthService {
    suspend fun attemptAuthentication(credential: Credential): Result<User, AuthError>
    fun signOut()
    suspend fun getSignedInUserIfAny(): Result<User, AuthError>
}

internal class FirebaseAuthService(private val auth: FirebaseAuth) : AuthService {
    override suspend fun getSignedInUserIfAny(): Result<User, AuthError> =
        runCatching { auth.currentUser }
            .mapError(::AuthError)
            .andThen { user ->
                user?.let { Ok(User(it)) }
                    ?: Err(AuthError("No currently signed in user"))
            }

    override suspend fun attemptAuthentication(credential: Credential): Result<User, AuthError> =
        when (credential) {
            is EmailPasswordCredential -> runCatching {
                auth.signInWithEmailAndPassword(credential.emailAddress, credential.password)
                    .await()
                    .user
            }
                .mapError(::AuthError)
                .andThen { user ->
                    user?.let { Ok(User(it)) }
                        ?: Err(AuthError("No currently signed in user"))
                }
        }

    override fun signOut() = auth.signOut()
}

data class AuthError(val message: String) {
    constructor(throwable: Throwable) : this(throwable.message ?: "Unknown error")
}

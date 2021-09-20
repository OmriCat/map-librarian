package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import com.google.firebase.auth.FirebaseAuth
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.UserUid
import kotlinx.coroutines.tasks.await

internal class FirebaseAuthService(private val auth: FirebaseAuth) : AuthService {
    override suspend fun getSignedInUserIfAny(): Result<User?, AuthError> =
        runCatching { auth.currentUser }
            .mapError(::AuthError)
            .andThen { user ->
                user?.let { Ok(FirebaseUser(it)) } ?: Ok(null)
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
                    user?.let { Ok(FirebaseUser(it)) }
                        ?: Err(AuthError("No currently signed in user"))
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

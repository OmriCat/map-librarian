package com.omricat.maplibrarian.firebase.auth

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.toResultOr
import com.google.firebase.auth.FirebaseAuth
import com.omricat.maplibrarian.auth.AuthError
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.Credential
import com.omricat.maplibrarian.auth.EmailPasswordCredential
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.UserUid
import com.omricat.maplibrarian.utils.DispatcherProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

public class FirebaseAuthService(
    private val auth: FirebaseAuth,
    private val dispatchers: DispatcherProvider = DispatcherProvider.Default
) : AuthService {
    override suspend fun getSignedInUserIfAny(): Result<User?, AuthError> =
        withContext(dispatchers.io) {
            runSuspendCatching { auth.currentUser }
                .mapError(::AuthError)
                .map { user -> user?.let { FirebaseUser(it) } }
        }

    override suspend fun attemptAuthentication(credential: Credential): Result<User, AuthError> =
        when (credential) {
            is EmailPasswordCredential ->
                withContext(dispatchers.io) {
                    runSuspendCatching {
                            auth
                                .signInWithEmailAndPassword(
                                    credential.emailAddress,
                                    credential.password
                                )
                                .await()
                                .user
                        }
                        .mapError(::AuthError)
                        .andThen { user -> // If user is null, no user is signed in
                            user
                                .toResultOr { AuthError("No currently signed in user") }
                                .map { FirebaseUser(it) }
                        }
                }
        }

    override suspend fun createUser(credential: Credential): Result<User, AuthError> =
        when (credential) {
            is EmailPasswordCredential ->
                withContext(dispatchers.io) {
                    runSuspendCatching {
                            auth
                                .createUserWithEmailAndPassword(
                                    credential.emailAddress,
                                    credential.password
                                )
                                .await()
                                .user
                        }
                        .mapError(::AuthError)
                        .andThen { maybeUser ->
                            maybeUser
                                .toResultOr { AuthError("User was created but not signed in") }
                                .map { FirebaseUser(it) }
                        }
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

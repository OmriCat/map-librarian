package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.toResultOr
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseUser as GoogleFirebaseUser

interface AuthService {
    fun attemptAuthentication(credential: Credential): Flow<Result<User, AuthError>>
    suspend fun signOut()
    fun getSignedInUserIfAny(): Flow<Result<User, AuthError>>
}

internal class FirebaseAuthService(private val auth: FirebaseAuth) : AuthService {
    override fun getSignedInUserIfAny(): Flow<Result<User, AuthError>> =
        flow {
            emit(
                auth.currentUser?.let { Ok(FirebaseUser(it)) }
                    ?: Err(AuthError("No currently signed in user"))
            )
        }.catchAndWrap()

    override fun attemptAuthentication(credential: Credential): Flow<Result<User, AuthError>> =
        when (credential) {
            is EmailPasswordCredential -> flow {
                emit(auth.signInWithEmailAndPassword(
                    credential.emailAddress,
                    credential.password
                ).await()
                    .user
                    .toResultOr { AuthError("Unknown email or password") }
                    .map { FirebaseUser(it) })
            }.catchAndWrap()
        }

    override suspend fun signOut() = auth.signOut()

    private fun <T> Flow<Result<T, AuthError>>.catchAndWrap() =
        catch { e -> emit(Err(AuthError(e.message ?: "Unknown error"))) }
}

@JvmInline
value class FirebaseUser(val user: GoogleFirebaseUser) : User {
    override val displayName: String
        get() = user.displayName ?: "(unknown name)"

    override val id: String
        get() = user.uid
}

data class AuthError(val message: String)

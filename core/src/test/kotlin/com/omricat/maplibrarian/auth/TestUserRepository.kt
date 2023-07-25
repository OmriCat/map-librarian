package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.model.User

internal class TestUserRepository(
    private val onAttemptAuthentication: (suspend (Credential) -> Result<User, AuthError>)? = null,
    private val onSignOut: (() -> Unit)? = null,
    private val onGetSignedInUserIfAny: (suspend () -> Result<User, AuthError>)? = null,
    private val onCreateUser: (suspend (Credential) -> Result<User, AuthError>)? = null
) : UserRepository {
    override suspend fun attemptAuthentication(credential: Credential): Result<User, AuthError> =
        onAttemptAuthentication?.invoke(credential)
            ?: Err(AuthError("Can't sign in with $credential"))

    override fun signOut() {
        onSignOut?.invoke()
    }

    override suspend fun getSignedInUserIfAny(): Result<User?, AuthError> =
        onGetSignedInUserIfAny?.invoke() ?: Ok(null)

    override suspend fun createUser(credential: Credential): Result<User, AuthError> =
        onCreateUser?.invoke(credential) ?: Err(AuthError("Can't create user with $credential"))
}

package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.auth.CreateUserError.OtherCreateUserError
import com.omricat.maplibrarian.model.User

internal class TestUserRepository(
    private val onAttemptAuthentication: (suspend (Credential) -> Result<User, MessageError>)? =
        null,
    private val onSignOut: (() -> Unit)? = null,
    private val onGetSignedInUserIfAny: (suspend () -> Result<User, MessageError>)? = null,
    private val onCreateUser: (suspend (Credential) -> Result<User, CreateUserError>)? = null
) : UserRepository {
    override suspend fun attemptAuthentication(credential: Credential): Result<User, MessageError> =
        onAttemptAuthentication?.invoke(credential)
            ?: Err(MessageError("Can't sign in with $credential"))

    override fun signOut() {
        onSignOut?.invoke()
    }

    override suspend fun getSignedInUserIfAny(): Result<User?, MessageError> =
        onGetSignedInUserIfAny?.invoke() ?: Ok(null)

    override suspend fun createUser(credential: Credential): Result<User, CreateUserError> =
        onCreateUser?.invoke(credential)
            ?: Err(OtherCreateUserError("Can't create user with $credential"))
}

package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlin.random.Random

interface AuthService {
    suspend fun attemptAuthentication(credential: Credential): Result<User, AuthError>
}

data class User(val emailAddress: String, val username: String)

@ExperimentalUnsignedTypes
class FakeAuthService : AuthService {
    override suspend fun attemptAuthentication(credential: Credential): Result<User, AuthError> =
        when (credential) {
            is EmailPasswordCredential ->
                if (credential.emailAddress != "root@example" && credential.password.isNotBlank()) {
                    Ok(
                        User(
                            credential.emailAddress,
                            Random.Default.nextInt().toUInt().toString(16)
                        )
                    )
                } else Err(AuthError("Unknown email address or password"))
            else ->
                Err(AuthError("Can only sign in with email address and password"))
        }
}

data class AuthError(val message: String)

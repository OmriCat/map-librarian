package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

interface AuthService {
    suspend fun attemptAuthentication(credential: Credential): Result<User, AuthError>
}

data class User(val emailAddress: String, val username: String)

private const val HEX_RADIX = 16

@ExperimentalTime
class FakeAuthService : AuthService {
    override suspend fun attemptAuthentication(credential: Credential): Result<User, AuthError> {
        delay(1.seconds)
        return when (credential) {
            is EmailPasswordCredential ->
                if (credential.emailAddress == "root@example" && credential.password.isNotBlank()) {
                    Ok(
                        User(
                            emailAddress = credential.emailAddress,
                            username = Random.Default.nextInt().toString(HEX_RADIX)
                        )
                    )
                } else Err(AuthError("Unknown email address or password"))
            else ->
                Err(AuthError("Can only sign in with email address and password"))
        }
    }
}

data class AuthError(val message: String)

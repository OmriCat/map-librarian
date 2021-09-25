package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.model.User

public data class AuthError(val message: String) {
    public constructor(throwable: Throwable) : this(throwable.message ?: "Unknown error")
}

public interface AuthService {
    public suspend fun attemptAuthentication(credential: Credential): Result<User, AuthError>
    public fun signOut()
    public suspend fun getSignedInUserIfAny(): Result<User?, AuthError>
}

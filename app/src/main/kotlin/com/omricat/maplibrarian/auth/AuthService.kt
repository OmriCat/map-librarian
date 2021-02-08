package com.omricat.maplibrarian.auth

import com.firebase.ui.auth.data.model.User
import com.github.michaelbull.result.Result

interface AuthService {
    suspend fun attemptAuthentication(credential: Credential): Result<User, AuthError>

    data class AuthError(val message: String)
}

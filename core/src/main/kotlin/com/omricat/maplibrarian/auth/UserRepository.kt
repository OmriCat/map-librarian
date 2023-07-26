package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.model.User

public data class MessageError(override val message: String) : UserRepository.Error {
    public constructor(throwable: Throwable) : this(throwable.message ?: "Unknown error")
}

public data class ExceptionWrapperError(val throwable: Throwable) : UserRepository.Error {
    override val message: String
        get() = throwable.message ?: "No message in $throwable"
}

public sealed class CreateUserError(override val message: String) : UserRepository.Error {
    public data class EmailAlreadyInUseError(public val emailAddress: String) :
        CreateUserError("Email address $emailAddress is already in use")

    public object WeakPasswordError : CreateUserError("Password is too weak")
    public object UserCreatedButSignedOutError :
        CreateUserError("User created but needs to sign in again")

    public data class OtherCreateUserError(public override val message: String) :
        CreateUserError(message)
}

public interface UserRepository {
    public suspend fun attemptAuthentication(credential: Credential): Result<User, Error>
    public fun signOut()
    public suspend fun getSignedInUserIfAny(): Result<User?, Error>
    public suspend fun createUser(credential: Credential): Result<User, CreateUserError>

    public sealed interface Error {
        public val message: String
    }
}

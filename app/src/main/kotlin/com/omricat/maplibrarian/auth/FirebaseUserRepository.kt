package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.toResultOr
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.omricat.firebase.interop.runCatchingFirebaseException
import com.omricat.maplibrarian.auth.CreateUserError.EmailAlreadyInUseError
import com.omricat.maplibrarian.auth.CreateUserError.UserCreatedButSignedOutError
import com.omricat.maplibrarian.auth.CreateUserError.WeakPasswordError
import com.omricat.maplibrarian.model.EmailAddress
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.UserUid
import com.omricat.maplibrarian.utils.DispatcherProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

public class FirebaseUserRepository(
    private val auth: FirebaseAuth,
    private val dispatchers: DispatcherProvider = DispatcherProvider.Default
) : UserRepository {
    override suspend fun getSignedInUserIfAny(): Result<User?, UserRepository.Error> =
        withContext(dispatchers.io) {
            runCatchingAuthExceptions { auth.currentUser }
                .mapError { e -> ExceptionWrapperError(e) }
                .map { user -> user?.let { FirebaseUser(it) } }
        }

    override suspend fun attemptAuthentication(
        credential: Credential
    ): Result<User, UserRepository.Error> =
        when (credential) {
            is EmailPasswordCredential ->
                withContext(dispatchers.io) {
                    runCatchingAuthExceptions {
                            auth
                                .signInWithEmailAndPassword(
                                    credential.emailAddress,
                                    credential.password
                                )
                                .await()
                                .user
                        }
                        .mapError(::ExceptionWrapperError)
                        .andThen { user -> // If user is null, no user is signed in
                            user
                                .toResultOr { MessageError("No currently signed in user") }
                                .map { FirebaseUser(it) }
                        }
                }
        }

    override suspend fun createUser(credential: Credential): Result<User, CreateUserError> =
        when (credential) {
            is EmailPasswordCredential ->
                withContext(dispatchers.io) {
                    runCatchingAuthExceptions {
                            auth
                                .createUserWithEmailAndPassword(
                                    credential.emailAddress,
                                    credential.password
                                )
                                .await()
                                .user
                        }
                        .mapError { e: FirebaseAuthException ->
                            when (e) {
                                is FirebaseAuthUserCollisionException ->
                                    EmailAlreadyInUseError(credential.emailAddress)
                                is FirebaseAuthWeakPasswordException -> WeakPasswordError
                                else -> throw e
                            }
                        }
                        .andThen { maybeUser ->
                            maybeUser
                                .toResultOr { UserCreatedButSignedOutError }
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
    override val emailAddress: EmailAddress
        get() {
            val email =
                user.email
                    ?: error(
                        """FirebaseUser email should always be non-null unless Multiple
                            | accounts per email has been enable in Firebase Console"""
                            .trimMargin()
                    )
            return EmailAddress(email)
        }
}

public inline fun <V> runCatchingAuthExceptions(block: () -> V): Result<V, FirebaseAuthException> =
    runCatchingFirebaseException(block)

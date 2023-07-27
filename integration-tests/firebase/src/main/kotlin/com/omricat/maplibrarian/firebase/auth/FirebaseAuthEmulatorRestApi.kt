package com.omricat.maplibrarian.firebase.auth

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.omricat.maplibrarian.auth.EmailPasswordCredential
import com.omricat.maplibrarian.model.EmailAddress
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.UserUid
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

class FirebaseAuthEmulatorRestApi(private val projectId: String, baseUrl: HttpUrl) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private interface AuthEmulatorApi {

        @DELETE("/emulator/v1/projects/{project-id}/accounts")
        fun deleteAllUsers(@Path("project-id") projectId: String): Call<Unit>

        @Headers("Authorization: Bearer owner")
        @POST("/identitytoolkit.googleapis.com/v1/projects/{project-id}/accounts")
        fun createUser(
            @Path("project-id") projectId: String,
            @Body body: RequestBody
        ): Call<TestUser>
    }

    @Serializable
    data class TestUser(override val displayName: String, val localId: String, val email: String) :
        User {
        override val id: UserUid = UserUid(localId)
        override val emailAddress: EmailAddress = EmailAddress(email)
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val wrappedApi: AuthEmulatorApi =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory(jsonMediaType))
            .build()
            .create()
    fun deleteAllUsers(): Response<Unit> = wrappedApi.deleteAllUsers(projectId).execute()

    fun createUser(credential: EmailPasswordCredential): Response<TestUser> {

        val jsonBody =
            """{
                "customAttributes":"",
                "displayName":"",
                "photoUrl":"",
                "email":"${credential.emailAddress}",
                "password":"${credential.password}",
                "phoneNumber":"",
                "emailVerified":false,
                "mfaInfo":[]
            }"""
                .toRequestBody(contentType = jsonMediaType)
        val call = wrappedApi.createUser(projectId, jsonBody)
        return call.execute()
    }
}

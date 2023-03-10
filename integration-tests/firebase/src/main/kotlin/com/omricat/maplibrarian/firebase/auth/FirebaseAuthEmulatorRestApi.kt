package com.omricat.maplibrarian.firebase.auth

import okhttp3.HttpUrl
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.DELETE
import retrofit2.http.Path

class FirebaseAuthEmulatorRestApi(private val projectId: String, baseUrl: HttpUrl) {

    private interface AuthEmulatorApi {

        @DELETE("/emulator/v1/projects/{project-id}/accounts")
        fun deleteAllUsers(@Path("project-id") projectId: String): Call<Unit>
    }

    private val wrappedApi: AuthEmulatorApi =
        Retrofit.Builder().baseUrl(baseUrl).build().create()
    fun deleteAllUsers(): Response<Unit> = wrappedApi.deleteAllUsers(projectId).execute()
}

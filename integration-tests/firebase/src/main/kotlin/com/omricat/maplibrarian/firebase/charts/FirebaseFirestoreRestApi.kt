package com.omricat.maplibrarian.firebase.charts

import android.util.Log
import com.omricat.maplibrarian.firebase.TestFixtures
import kotlin.time.ExperimentalTime
import okhttp3.HttpUrl
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.DELETE
import retrofit2.http.Path

@ExperimentalTime
class FirebaseFirestoreRestApi(private val projectId: String, baseUrl: HttpUrl) {

    val events: (String) -> Unit = { message ->
        Log.d(FirebaseFirestoreRestApi::class.simpleName, message)
    }

    private interface FirestoreEmulatorApi {

        @DELETE("/emulator/v1/projects/{project-id}/databases/(default)/documents")
        fun deleteDefaultDatabase(@Path("project-id") projectId: String): Call<Unit>
    }

    private val wrappedApi: FirestoreEmulatorApi =
        Retrofit.Builder()
            .client(TestFixtures.okHttpClient(events))
            .baseUrl(baseUrl)
            .build()
            .create()

    fun deleteAllData(): Response<Unit> = wrappedApi.deleteDefaultDatabase(projectId).execute()
}

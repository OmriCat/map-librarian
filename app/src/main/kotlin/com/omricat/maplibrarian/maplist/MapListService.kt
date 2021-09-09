package com.omricat.maplibrarian.maplist

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import com.google.firebase.firestore.FirebaseFirestore
import com.omricat.maplibrarian.Map
import com.omricat.maplibrarian.MapId
import com.omricat.maplibrarian.User
import kotlinx.coroutines.tasks.await

interface MapListService {

    suspend fun mapsListForUser(user: User): Result<List<Map>, MapListError>
}

internal class FirebaseMapListService(private val db: FirebaseFirestore) : MapListService {
    override suspend fun mapsListForUser(user: User): Result<List<Map>, MapListError> =
        runCatching {
            db.collection("maps")
                .whereEqualTo("userId", user.id.id)
                .get()
                .await()
        }
            .mapError(MapListError::fromThrowable)
            .andThen {
                Ok(it.map { m ->
                    Map(
                        mapId = MapId(m.id), title = m["title"] as? String ?: "(Untitled)"
                    )
                })
            }
}

data class MapListError(val message: String) {
    private constructor(throwable: Throwable) : this(throwable.message ?: "Unknown error")

    companion object {
        fun fromThrowable(throwable: Throwable) = MapListError(throwable)
    }
}

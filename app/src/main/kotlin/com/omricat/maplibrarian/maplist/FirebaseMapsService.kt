package com.omricat.maplibrarian.maplist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.omricat.maplibrarian.model.DbMapModel
import com.omricat.maplibrarian.model.MapId
import com.omricat.maplibrarian.model.MapModel
import com.omricat.maplibrarian.model.User
import kotlinx.coroutines.tasks.await

class FirebaseMapsService(private val db: FirebaseFirestore) : MapsService {
    override suspend fun mapsListForUser(user: User): Result<List<DbMapModel>, MapsServiceError> =
        runCatching {
            db.collection("maps")
                .whereEqualTo("userId", user.id.id)
                .get()
                .await()
        }
            .mapError(MapsServiceError::fromThrowable)
            .map {
                it.map { m: DocumentSnapshot -> m.toMapModel() }
            }

    override suspend fun addNewMap(
        user: User,
        newMap: MapModel
    ): Result<DbMapModel, MapsServiceError> = runCatching {
        db.collection("maps")
            .add(newMap.toStringMap())
            .await()
    }.mapError(MapsServiceError::fromThrowable)
        .map { ref -> DbMapModel(MapId(ref.id), newMap) }
}

internal fun DocumentSnapshot.toMapModel(): DbMapModel =
    DbMapModel(mapId = MapId(id), title = get("title") as? CharSequence ?: "Error getting title")

internal fun MapModel.toStringMap(): Map<String, Any> =
    mapOf("title" to title)

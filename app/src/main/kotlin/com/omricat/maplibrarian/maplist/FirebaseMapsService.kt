package com.omricat.maplibrarian.maplist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.combine
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.omricat.maplibrarian.model.DbMapModel
import com.omricat.maplibrarian.model.DbMapModelDeserializer
import com.omricat.maplibrarian.model.MapId
import com.omricat.maplibrarian.model.MapModel
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.serialized
import com.omricat.maplibrarian.utils.DispatcherProvider
import com.omricat.maplibrarian.utils.logErrorAndMap
import com.omricat.maplibrarian.utils.runSuspendCatching
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseMapsService(
    private val db: FirebaseFirestore,
    private val dispatchers: DispatcherProvider = DispatcherProvider.Default
) : MapsService {

    override suspend fun mapsListForUser(user: User): Result<List<DbMapModel>, MapsServiceError> =
        withContext(dispatchers.io) {
            runSuspendCatching {
                db.mapsCollection(user)
                    .get()
                    .await()
            }
        }.mapError(MapsServiceError::fromThrowable)
            .andThen { snapshot ->
                snapshot.map { m: DocumentSnapshot -> m.parseMapModel() }.combine()
                    .mapError { MapsServiceError(it.message) }
            }

    override suspend fun addNewMap(
        user: User,
        newMap: MapModel
    ): Result<DbMapModel, MapsServiceError> {
        require(user.id == newMap.userId) {
            "UserId of newMap (was ${newMap.userId}) must be " +
                "same as userId of user (was ${user.id})"
        }
        return withContext(dispatchers.io) {
            runSuspendCatching {
                db.mapsCollection(user)
                    .add(newMap.serialized())
                    .await()
            }
        }.logErrorAndMap(MapsServiceError::fromThrowable)
            .map { ref -> DbMapModel(MapId(ref.id), newMap) }
    }

    private fun FirebaseFirestore.mapsCollection(user: User) =
        collection("users")
            .document(user.id.toString())
            .collection("maps")
}

internal fun DocumentSnapshot.parseMapModel() =
    DbMapModelDeserializer(id, data ?: emptyMap())

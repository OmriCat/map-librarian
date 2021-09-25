package com.omricat.maplibrarian.maplist

import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.model.DbMapModel
import com.omricat.maplibrarian.model.MapModel
import com.omricat.maplibrarian.model.User

public interface MapsService {
    public suspend fun mapsListForUser(user: User): Result<List<DbMapModel>, MapsServiceError>
    public suspend fun addNewMap(user: User, newMap: MapModel): Result<DbMapModel, MapsServiceError>
}

public data class MapsServiceError(val message: String) {
    private constructor(throwable: Throwable) : this(throwable.message ?: "Unknown error")

    public companion object {
        public fun fromThrowable(throwable: Throwable): MapsServiceError =
            MapsServiceError(throwable)
    }
}

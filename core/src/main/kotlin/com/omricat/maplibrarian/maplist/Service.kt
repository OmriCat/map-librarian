package com.omricat.maplibrarian.maplist

import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.model.Map
import com.omricat.maplibrarian.model.User

public interface MapListService {

    public suspend fun mapsListForUser(user: User): Result<List<Map>, MapListError>
}

public data class MapListError(val message: String) {
    private constructor(throwable: Throwable) : this(throwable.message ?: "Unknown error")

    public companion object {
        public fun fromThrowable(throwable: Throwable): MapListError = MapListError(throwable)
    }
}

package com.omricat.maplibrarian.maplist

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.auth.User
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

interface MapListService {

    suspend fun mapsListForUser(user: User): Result<List<Map>, MapListError>
}

@ExperimentalTime
internal class FakeMapListService : MapListService {
    override suspend fun mapsListForUser(user: User): Result<List<Map>, MapListError> {
        delay(Duration.seconds(2))
        return Ok(emptyList())
    }
}

data class MapListError(val message: String) {
    constructor(throwable: Throwable) : this(throwable.message ?: "Unknown error")
}

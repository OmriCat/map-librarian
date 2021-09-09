package com.omricat.maplibrarian.maplist

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.Map
import com.omricat.maplibrarian.MapId
import com.omricat.maplibrarian.User
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
        return Ok(listOf(
            Map(MapId("3NcI4YL1QjPPsUa0pVKW"), "Alnwick & Morpeth"),
            Map(MapId("12345"), "Edinburgh"),
        ))
    }
}

data class MapListError(val message: String) {
    private constructor(throwable: Throwable) : this(throwable.message ?: "Unknown error")
    companion object {
        fun fromThrowable(throwable: Throwable) = MapListError(throwable)
    }
}

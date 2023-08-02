package com.omricat.maplibrarian.chartlist

import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.maplibrarian.model.User
import kotlinx.serialization.Serializable

public interface ChartsRepository {
    public suspend fun chartsListForUser(
        user: User
    ): Result<List<DbChartModel>, ChartsRepository.Error>
    public suspend fun addNewChart(
        user: User,
        newChart: UnsavedChartModel
    ): Result<DbChartModel, ChartsRepository.Error>
    public sealed interface Error {
        public val message: String
    }
}

// public typealias ChartsServiceError = ChartsService.Error
@Serializable
public data class ChartsServiceError(override val message: String) : ChartsRepository.Error {
    private constructor(throwable: Throwable) : this(throwable.message ?: "Unknown error")

    public companion object {
        public fun fromThrowable(throwable: Throwable): ChartsServiceError =
            ChartsServiceError(throwable)
    }
}

package com.omricat.maplibrarian.chartlist

import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.model.ChartModel
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.User
import kotlinx.serialization.Serializable

public interface ChartsService {
    public suspend fun chartsListForUser(user: User): Result<List<DbChartModel>, ChartsServiceError>
    public suspend fun <T : ChartModel<T>> saveChart(
        user: User,
        newChart: ChartModel<T>
    ): Result<DbChartModel, ChartsServiceError>
}

@Serializable
public data class ChartsServiceError(val message: String) {
    private constructor(throwable: Throwable) : this(throwable.message ?: "Unknown error")

    public companion object {
        public fun fromThrowable(throwable: Throwable): ChartsServiceError =
            ChartsServiceError(throwable)
    }
}

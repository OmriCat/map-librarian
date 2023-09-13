package com.omricat.maplibrarian.chartlist

import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.maplibrarian.model.User

public interface ChartsRepository {
    public suspend fun chartsListForUser(
        user: User
    ): Result<List<DbChartModel>, ChartsRepository.Error>

    public suspend fun addNewChart(
        user: User,
        newChart: UnsavedChartModel
    ): Result<DbChartModel, AddNewChartError>
    public interface Error {
        public val message: String

        public data class MessageError(override val message: String) : Error

        public data class ExceptionWrappingError(public val exception: Throwable) : Error {
            override val message: String
                get() = exception.message ?: "No message in exception $exception"
        }
    }

    public sealed class AddNewChartError(public val message: String) {
        public object Unavailable : AddNewChartError("Service temporarily unavailable")

        public object Cancelled : AddNewChartError("Operation ")
        public data class ChartExists(public val unsavedChartModel: UnsavedChartModel) :
            AddNewChartError("Chart already exists: $unsavedChartModel")
        public data class OtherException(val exception: Throwable) :
            AddNewChartError(exception.message ?: "No message in $exception")
    }
}

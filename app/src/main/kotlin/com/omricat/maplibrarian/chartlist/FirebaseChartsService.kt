package com.omricat.maplibrarian.chartlist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.combine
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.omricat.maplibrarian.model.ChartId
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.DbChartModelFromMapDeserializer
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.serializedToMap
import com.omricat.maplibrarian.utils.DispatcherProvider
import com.omricat.maplibrarian.utils.logErrorAndMap
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class FirebaseChartsService(
    private val db: FirebaseFirestore,
    private val dispatchers: DispatcherProvider = DispatcherProvider.Default
) : ChartsService {

    override suspend fun chartsListForUser(
        user: User
    ): Result<List<DbChartModel>, ChartsServiceError> =
        withContext(dispatchers.io) { runSuspendCatching { db.mapsCollection(user).get().await() } }
            .mapError(ChartsServiceError::fromThrowable)
            .onFailure { Timber.e(it.message) }
            .andThen { snapshot ->
                snapshot
                    .map { m -> m.parseMapModel() }
                    .combine()
                    .mapError { e -> ChartsServiceError(e.message) }
            }

    override suspend fun addNewChart(
        user: User,
        newChart: UnsavedChartModel
    ): Result<DbChartModel, ChartsServiceError> {
        require(user.id == newChart.userId) {
            "UserId of newMap (was ${newChart.userId}) must be " +
                "same as userId of user (was ${user.id})"
        }
        return withContext(dispatchers.io) {
                runSuspendCatching {
                    db.mapsCollection(user).add(newChart.serializedToMap()).await()
                }
            }
            .logErrorAndMap(ChartsServiceError::fromThrowable)
            .map { ref -> newChart.withChartId(ChartId(ref.id)) }
    }

    private fun FirebaseFirestore.mapsCollection(user: User) =
        collection("users").document(user.id.value).collection("maps")
}

/*
   It is always safe to upcast ChartModel<Nothing?> to ChartModel<ChartId?> since the only
   possible value for a val of type Nothing? is null.

   It is safe to cast ChartModel<ChartId?> to ChartModel<ChartId> immediately after setting
   the chartId parameter to a non-null value.
*/
@Suppress("UNCHECKED_CAST")
private fun UnsavedChartModel.withChartId(chartId: ChartId): DbChartModel =
    DbChartModel(userId = userId, title = title, chartId = chartId)

internal fun DocumentSnapshot.parseMapModel() =
    DbChartModelFromMapDeserializer(id, data ?: emptyMap())

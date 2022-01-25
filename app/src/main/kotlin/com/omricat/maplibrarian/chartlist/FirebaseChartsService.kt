package com.omricat.maplibrarian.chartlist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.combine
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.omricat.maplibrarian.model.ChartId
import com.omricat.maplibrarian.model.ChartModel
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.DbChartModelFromMapDeserializer
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.serializedToMap
import com.omricat.maplibrarian.utils.DispatcherProvider
import com.omricat.maplibrarian.utils.logErrorAndMap
import com.omricat.maplibrarian.utils.runSuspendCatching
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseChartsService(
    private val db: FirebaseFirestore,
    private val dispatchers: DispatcherProvider = DispatcherProvider.Default
) : ChartsService {

    override suspend fun chartsListForUser(user: User):
        Result<List<DbChartModel>, ChartsServiceError> =
        withContext(dispatchers.io) {
            runSuspendCatching {
                db.mapsCollection(user)
                    .get()
                    .await()
            }
        }
            .mapError(ChartsServiceError::fromThrowable)
            .andThen { snapshot ->
                snapshot.map { m -> m.parseMapModel() }.combine()
                    .mapError { e -> ChartsServiceError(e.message) }
            }

    override suspend fun <T : ChartModel<T>> saveChart(
        user: User,
        newChart: ChartModel<T>
    ): Result<DbChartModel, ChartsServiceError> {
        require(user.id == newChart.userId) {
            "UserId of newMap (was ${newChart.userId}) must be " +
                "same as userId of user (was ${user.id})"
        }
        return withContext(dispatchers.io) {
            when (newChart) {
                is UnsavedChartModel -> runSuspendCatching {
                    db.mapsCollection(user)
                        .add(newChart.serializedToMap())
                        .await()
                }.map { ref ->
                    newChart.withChartId(ChartId(ref.id))
                }

                is DbChartModel -> runSuspendCatching {
                    db.mapsCollection(user)
                        .document(newChart.chartId.id)
                        .set(newChart.serializedToMap())
                        .await()
                }.map { newChart }
            }.logErrorAndMap(ChartsServiceError::fromThrowable)
        }
    }

    private fun FirebaseFirestore.mapsCollection(user: User) =
        collection("users")
            .document(user.id.id)
            .collection("maps")
}

private fun UnsavedChartModel.withChartId(chartId: ChartId): DbChartModel =
    DbChartModel(
        userId = userId,
        title = title,
        chartId = chartId
    )

internal fun DocumentSnapshot.parseMapModel() =
    DbChartModelFromMapDeserializer(id, data ?: emptyMap())

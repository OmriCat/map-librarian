package com.omricat.maplibrarian.chartlist

import co.touchlab.kermit.Severity.Warn
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.combine
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code.ALREADY_EXISTS
import com.google.firebase.firestore.FirebaseFirestoreException.Code.CANCELLED
import com.google.firebase.firestore.FirebaseFirestoreException.Code.DATA_LOSS
import com.google.firebase.firestore.FirebaseFirestoreException.Code.INTERNAL
import com.google.firebase.firestore.FirebaseFirestoreException.Code.OK
import com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE
import com.google.firebase.firestore.FirebaseFirestoreException.Code.UNKNOWN
import com.google.firebase.firestore.QuerySnapshot
import com.omricat.firebase.interop.runCatchingFirebaseException
import com.omricat.logging.Loggable
import com.omricat.logging.Logger
import com.omricat.logging.log
import com.omricat.maplibrarian.chartlist.ChartsRepository.AddNewChartError
import com.omricat.maplibrarian.chartlist.ChartsRepository.AddNewChartError.Cancelled
import com.omricat.maplibrarian.chartlist.ChartsRepository.AddNewChartError.ChartExists
import com.omricat.maplibrarian.chartlist.ChartsRepository.AddNewChartError.OtherException
import com.omricat.maplibrarian.chartlist.ChartsRepository.AddNewChartError.Unavailable
import com.omricat.maplibrarian.chartlist.ChartsRepository.Error.ExceptionWrappingError
import com.omricat.maplibrarian.chartlist.ChartsRepository.Error.MessageError
import com.omricat.maplibrarian.model.ChartId
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.DbChartModelFromMapDeserializer
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.serializedToMap
import com.omricat.maplibrarian.utils.DispatcherProvider
import com.omricat.maplibrarian.utils.logAndMapException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseChartsRepository(
    private val db: FirebaseFirestore,
    private val dispatchers: DispatcherProvider = DispatcherProvider.Default,
    override val logger: Logger
) : ChartsRepository, Loggable {
    override suspend fun chartsListForUser(
        user: User
    ): Result<List<DbChartModel>, ChartsRepository.Error> =
        withContext(dispatchers.io) {
                runCatchingFirestoreException<QuerySnapshot> {
                    db.mapsCollection(user).get().await()
                }
            }
            .onFailure { log(Warn, throwable = it) }
            .mapError(::ExceptionWrappingError)
            .andThen { snapshot ->
                snapshot
                    .map { m -> m.parseMapModel() }
                    .combine()
                    .mapError { e -> MessageError(e.message) }
            }

    override suspend fun addNewChart(
        user: User,
        newChart: UnsavedChartModel
    ): Result<DbChartModel, AddNewChartError> {
        require(user.id == newChart.userId) {
            "UserId of newMap (was ${newChart.userId}) must be " +
                "same as userId of user (was ${user.id})"
        }
        return withContext(dispatchers.io) {
                runCatchingFirestoreException {
                    db.mapsCollection(user).add(newChart.serializedToMap()).await()
                }
            }
            .logAndMapException { exception ->
                when (exception.code) {
                    UNAVAILABLE -> Unavailable
                    ALREADY_EXISTS -> ChartExists(newChart)
                    CANCELLED -> Cancelled
                    INTERNAL -> error("Firebase threw an internal error. This is unrecoverable.")
                    DATA_LOSS -> error("Firebase indicated unrecoverable data loss or corruption")
                    OK ->
                        error(
                            "FirebaseFirestoreException $exception had a status code of OK. " +
                                "Docs say this should never happen."
                        )
                    UNKNOWN -> OtherException(exception)
                    else -> OtherException(exception)
                }
            }
            .onFailure { log(Warn) { "error Adding New Chart: ${it.message}" } }
            .map { ref -> newChart.withChartId(ChartId(ref.id)) }
    }

    private fun FirebaseFirestore.mapsCollection(user: User) =
        collection("users").document(user.id.value).collection("maps")
}

private fun UnsavedChartModel.withChartId(chartId: ChartId): DbChartModel =
    DbChartModel(userId = userId, title = title, chartId = chartId)

internal fun DocumentSnapshot.parseMapModel() =
    DbChartModelFromMapDeserializer(id, data ?: emptyMap())

private inline fun <V> runCatchingFirestoreException(
    block: () -> V
): Result<V, FirebaseFirestoreException> = runCatchingFirebaseException(block)

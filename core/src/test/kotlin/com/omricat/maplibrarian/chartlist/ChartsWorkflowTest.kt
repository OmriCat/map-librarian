package com.omricat.maplibrarian.chartlist

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.tableOf
import com.omricat.maplibrarian.model.ChartId
import com.omricat.maplibrarian.model.DbChartModel
import kotlin.test.Test
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Nested

internal class ChartsWorkflowTest {
    @Nested
    inner class ChartsWorkflowStateSnapshotterTest {

        private val snapshotter = ChartsWorkflowState.snapshotter(Json)

        @Test
        fun `Snapshot round trips are the identity`() {

            tableOf("State")
                .row<ChartsWorkflowState>(ChartsWorkflowState.RequestData)
                .row(ChartsWorkflowState.AddingItem)
                .row(
                    ChartsWorkflowState.ChartsListLoaded(
                        listOf(DbChartModel("title", ChartId("chart")))
                    )
                )
                .forAll { state ->
                    assertThat(snapshotter.valueFromSnapshot(snapshotter.snapshotOf(state)))
                        .isEqualTo(state)
                }
        }

        @Test
        fun `transforms ErrorLoadingCharts to RequestData`() {
            val state =
                ChartsWorkflowState.ErrorLoadingCharts(
                    ChartsRepository.Error.MessageError("Error message")
                )

            assertThat(snapshotter.valueFromSnapshot(snapshotter.snapshotOf(state)))
                .isInstanceOf<ChartsWorkflowState.RequestData>()
        }
    }
}

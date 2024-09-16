package com.omricat.maplibrarian.chartlist

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import com.omricat.maplibrarian.chartlist.ChartDetailsWorkflowImpl.State
import com.omricat.maplibrarian.model.ChartId
import com.omricat.maplibrarian.model.DbChartModel
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChartDetailsWorkflowTest {

    private val snapshotter = State.snapshotter(Json)

    @Nested
    inner class StateSnapshotterTest {
        @Test
        fun `Snapshot round trips are the identity`() {
            val chart = DbChartModel("title", ChartId("chartId"))
            tableOf("state")
                .row<State>(State.ShowingDetails(chart))
                .row(State.EditingChart(chart))
                .forAll { state ->
                    assertThat(snapshotter.valueFromSnapshot(snapshotter.snapshotOf(state)))
                        .isEqualTo(state)
                }
        }
    }
}

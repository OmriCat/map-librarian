package com.omricat.maplibrarian.chartlist

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow.State
import com.omricat.maplibrarian.model.UnsavedChartModel
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AddNewChartWorkflowTest {
    @Nested
    inner class StateSnapshotterTest {
        private val chart = UnsavedChartModel("title")
        private val snapshotter = State.snapshotter(Json)

        @Test
        fun `Snapshot round trips are the identity`() {
            tableOf("state")
                .row<State>(State.Editing(chart, "error message"))
                .row(State.Saving(chart))
                .forAll { state ->
                    assertThat(snapshotter.valueFromSnapshot(snapshotter.snapshotOf(state)))
                        .isEqualTo(state)
                }
        }
    }
}

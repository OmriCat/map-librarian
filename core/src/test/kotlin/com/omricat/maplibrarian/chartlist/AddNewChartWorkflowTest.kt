package com.omricat.maplibrarian.chartlist

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow.State
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.maplibrarian.model.UserUid
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AddNewChartWorkflowTest {
    @Nested
    inner class ToSnapshotFromSnapshotRoundTrip {
        private val chart = UnsavedChartModel(UserUid("user"), "title")

        @Test
        fun `is the identity for State#Editing`() {
            val state = State.Editing(chart, "error message")

            assertThat(State.fromSnapshot(state.toSnapshot())).isEqualTo(state)
        }

        @Test
        fun `is the identity for State#Saving`() {
            val state = State.Saving(chart)

            assertThat(State.fromSnapshot(state.toSnapshot())).isEqualTo(state)
        }
    }
}

package com.omricat.maplibrarian.chartlist

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.omricat.maplibrarian.model.ChartId
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.UserUid
import kotlin.test.Test
import org.junit.jupiter.api.Nested

internal class ChartsWorkflowTest {
    @Nested
    inner class ToSnapshotFromSnapshotRoundTrip {
        @Test
        fun `is the identity for RequestData`() {
            val state = ChartsWorkflowState.RequestData

            assertThat(ChartsWorkflowState.fromSnapshot(state.toSnapshot())).isEqualTo(state)
        }

        @Test
        fun `is the identity for AddingItem`() {
            val state = ChartsWorkflowState.AddingItem

            assertThat(ChartsWorkflowState.fromSnapshot(state.toSnapshot())).isEqualTo(state)
        }

        @Test
        fun `is the identity for ChartsListLoaded`() {
            val state =
                ChartsWorkflowState.ChartsListLoaded(
                    listOf(DbChartModel(UserUid("user"), "title", ChartId("chart")))
                )

            assertThat(ChartsWorkflowState.fromSnapshot(state.toSnapshot())).isEqualTo(state)
        }

        @Test
        fun `transforms ErrorLoadingCharts to RequestData`() {
            val state =
                ChartsWorkflowState.ErrorLoadingCharts(
                    ChartsRepository.Error.MessageError("Error message")
                )

            assertThat(ChartsWorkflowState.fromSnapshot(state.toSnapshot()))
                .isInstanceOf<ChartsWorkflowState.RequestData>()
        }
    }
}

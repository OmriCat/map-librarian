package com.omricat.maplibrarian.chartlist

import com.omricat.maplibrarian.model.ChartId
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.UserUid
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

internal class ChartsWorkflowTest : WordSpec({

    "ChartsState.toSnapshot composed with fromSnapshot" should {
        val dbChart = DbChartModel(
            UserUid("user"), "title",
            ChartId("chart")
        )
        "be the identity for RequestData" {
            val state = ChartsWorkflowState.RequestData

            ChartsWorkflowState.fromSnapshot(state.toSnapshot()) shouldBe state
        }

        "be the identity for AddingItem" {
            val state = ChartsWorkflowState.AddingItem

            ChartsWorkflowState.fromSnapshot(state.toSnapshot()) shouldBe state
        }

        "be the identity for ChartsListLoaded" {
            val state = ChartsWorkflowState.ChartsListLoaded(listOf(dbChart))

            ChartsWorkflowState.fromSnapshot(state.toSnapshot()) shouldBe state
        }

        "transform ErrorLoadingCharts to RequestData" {
            val state = ChartsWorkflowState.ErrorLoadingCharts(ChartsServiceError("Error message"))

            ChartsWorkflowState.fromSnapshot(state.toSnapshot()) shouldBe
                ChartsWorkflowState.RequestData
        }

        "be the identity for ShowingDetails" {
            val state = ChartsWorkflowState.ShowingDetails(dbChart)

            ChartsWorkflowState.fromSnapshot(state.toSnapshot()) shouldBe state
        }
    }
})

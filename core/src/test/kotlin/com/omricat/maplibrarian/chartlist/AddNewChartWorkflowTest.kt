package com.omricat.maplibrarian.chartlist

import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow.State
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.maplibrarian.model.UserUid
import com.omricat.workflow.StateSnapshotSerializer
import com.squareup.workflow1.Snapshot
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

internal class AddNewChartWorkflowTest : WordSpec({

    "AddNewChartWorkflow.State#toSnapshot composed with fromSnapshot" should {

        val stateSerializer = State.snapshotSerializer(StateSnapshotSerializer.json)

        fun State.toSnapshot() = stateSerializer.toSnapshot(this)
        fun State.Companion.fromSnapshot(snapshot: Snapshot) =
            stateSerializer.fromSnapshot(snapshot)

        val chart = UnsavedChartModel(UserUid("user"), "title")

        "be the identity for State.Editing" {
            val state = State.Editing(chart, "error message")

            State.fromSnapshot(state.toSnapshot()) shouldBe state
        }

        "be the identity for State.Saving" {
            val state = State.Saving(chart)

            State.fromSnapshot(state.toSnapshot()) shouldBe state
        }
    }
})

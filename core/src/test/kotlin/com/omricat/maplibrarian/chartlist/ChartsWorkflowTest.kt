package com.omricat.maplibrarian.chartlist

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

internal class ChartsWorkflowTest : WordSpec({

    "ChartsState.toSnapshot composed with fromSnapshot" should {
        "be the identity for RequestData" {
            val state = ChartsState.RequestData

            ChartsState.fromSnapshot(state.toSnapshot()) shouldBe state
        }
    }

})

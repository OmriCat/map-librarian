package com.omricat.maplibrarian.chartlist

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.omricat.maplibrarian.chartlist.DeserializerError.PropertyNonFoundError
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.result.assertk.isErr
import com.omricat.result.assertk.isOk
import kotlin.test.Test

internal class ChartModelSerializationTest {

    @Test
    fun `serializing then deserializing should be identity`() {
        val chartModel = UnsavedChartModel("Chart title")
        val result =
            ChartModelFromMapDeserializer.deserializeFromMap(
                ChartModelToMapSerializer.serializeToMap(chartModel)
            )
        assertThat(result).isOk().isEqualTo(chartModel)
    }

    @Test
    fun `deserializing should fail if any property is missing`() {
        val serialized: Map<String, Any> =
            ChartModelToMapSerializer.serializeToMap(UnsavedChartModel("Chart title"))

        val deserializer = ChartModelFromMapDeserializer

        assertAll {
            serialized
                .map { (key, _) -> serialized.toMutableMap().minus(key) }
                .forEach { map ->
                    val result = deserializer.deserializeFromMap(map)
                    assertThat(result).isErr().isInstanceOf<PropertyNonFoundError>()
                }
        }
    }
}

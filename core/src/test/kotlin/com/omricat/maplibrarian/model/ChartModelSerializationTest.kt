package com.omricat.maplibrarian.model

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.each
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.prop
import com.omricat.maplibrarian.model.serialization.DeserializerError.CastError
import com.omricat.maplibrarian.model.serialization.DeserializerError.PropertyNonFoundError
import com.omricat.result.assertk.isErr
import com.omricat.result.assertk.isOk
import kotlin.test.Test

internal class ChartModelSerializationTest {
    @kotlin.test.Test
    fun `serializing chart model gives a map with correct keys`() {
        val testMap = UnsavedChartModel(userId = UserUid("user1"), title = "A nice map")

        val serialized: Map<String, Any?> = testMap.serializedToMap()

        assertThat(serialized.keys).containsExactlyInAnyOrder("title", "userId")

        //            serialized.keys shouldContainExactly setOf("title", "userId")
    }

    @Test
    fun `serializing chart model gives a map with all string values`() {
        val testMap = UnsavedChartModel(userId = UserUid("user1"), title = "A nice map")

        val serialized: Map<String, Any?> = testMap.serializedToMap()

        assertThat(serialized.values).each { it.isNotNull().isInstanceOf<String>() }

        //            serialized.values.forEach { it.shouldBeTypeOf<String>() }
    }

    @Test
    fun `deserializing from Map(String, Any) works correctly if all properties found`() {
        val serialized = mapOf("title" to "A nice map", "userId" to "user1")
        val id = "map1"

        val deserialized = DbChartModelFromMapDeserializer(id, serialized)

        assertThat(deserialized).isOk().isInstanceOf<DbChartModel>()

        //            deserialized.shouldBeTypeOf<Ok<DbChartModel>>()
    }

    @Test
    fun `deserializing should fail if title property is missing`() {
        val serialized = mapOf("userId" to "user1")
        val id = "map1"

        val deserialized = DbChartModelFromMapDeserializer(id, serialized)

        assertThat(deserialized)
            .isErr()
            .isInstanceOf<PropertyNonFoundError>()
            .prop("message") { it.message }
            .contains("Property title not found")

        //            deserialized.shouldBeTypeOf<Err<DeserializerError.PropertyNonFoundError>>()
        //            deserialized.error.message shouldContain "Property title not found"
    }

    @Test
    fun `deserializing should fail if userId property cannot be converted to UserUid type`() {
        val serialized = mapOf("title" to "A nice map", "userId" to 1.0)
        val id = "map1"

        val deserialized = DbChartModelFromMapDeserializer(id, serialized)

        assertThat(deserialized)
            .isErr()
            .isInstanceOf<CastError>()
            .prop("message") { it.message }
            .contains("Can't cast")

        //            deserialized.shouldBeTypeOf<Err<DeserializerError.CastError>>()
        //            deserialized.error.message shouldContain "Can't cast"
    }
}

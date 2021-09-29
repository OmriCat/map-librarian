package com.omricat.maplibrarian.model

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeTypeOf

internal class MapModelSerializationTest : StringSpec({

    "serializing map model gives a map with correct keys" {
        val testMap =
            DbMapModel(
                mapId = MapId("map1"),
                userId = UserUid("user1"),
                title = "A nice map"
            )

        val serialized: Map<String, Any?> = testMap.serialized()

        serialized.keys shouldContainExactly setOf("title", "userId")
    }

    "deserializing from Map<String, Any> works correctly if all properties found" {
        val serialized = mapOf("title" to "A nice map", "userId" to "user1")
        val id = "map1"

        val deserialized = DbMapModelDeserializer(id, serialized)

        deserialized.shouldBeTypeOf<Ok<DbMapModel>>()
    }

    "deserializing should fail if title property is missing" {
        val serialized = mapOf("userId" to "user1")
        val id = "map1"

        val deserialized = DbMapModelDeserializer(id, serialized)

        deserialized.shouldBeTypeOf<Err<DbMapModelDeserializer.Error>>()
        deserialized.error.message shouldContain "Property title not found"
    }

    "deserializing should fail if userId property cannot be converted to UserUid type" {
        val serialized = mapOf("title" to "A nice map", "userId" to 1.0)
        val id = "map1"

        val deserialized = DbMapModelDeserializer(id, serialized)

        deserialized.shouldBeTypeOf<Err<DbMapModelDeserializer.Error>>()
        deserialized.error.message shouldContain "Can't cast property userId"
    }
})

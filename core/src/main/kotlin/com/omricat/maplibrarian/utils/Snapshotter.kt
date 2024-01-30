package com.omricat.maplibrarian.utils

import com.squareup.workflow1.Snapshot
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat

public abstract class Snapshotter<T>(
    private val stringFormat: StringFormat,
    private val serializationStrategy: SerializationStrategy<T>,
    private val deserializationStrategy: DeserializationStrategy<T>,
) {
    public constructor(
        stringFormat: StringFormat,
        serializer: KSerializer<T>,
    ) : this(
        stringFormat,
        serializer,
        serializer,
    )

    public fun snapshotOf(value: T): Snapshot =
        Snapshot.of(stringFormat.encodeToString(serializationStrategy, preSerialization(value)))

    public fun valueFromSnapshot(snapshot: Snapshot): T =
        postDeserialization(
            stringFormat.decodeFromString(deserializationStrategy, (snapshot.bytes.utf8()))
        )

    public open fun preSerialization(value: T): T = value

    public open fun postDeserialization(deserializedValue: T): T = deserializedValue
}

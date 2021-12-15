package com.omricat.workflow

import com.omricat.maplibrarian.model.ChartModel
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json

public class StateSnapshotSerializer<T : Any> internal constructor(
    private val serializationStrategy: SerializationStrategy<T>,
    private val deserializationStrategy: DeserializationStrategy<T>,
    private val serializationFormat: StringFormat
) {
    internal constructor(
        serializationStrategy: KSerializer<T>,
        serializationFormat: StringFormat
    ) : this(
        serializationStrategy,
        serializationStrategy,
        serializationFormat
    )

    public fun fromSnapshot(snapshot: Snapshot): T =
        serializationFormat.decodeFromString(deserializationStrategy, snapshot.bytes.utf8())

    public fun toSnapshot(value: T): Snapshot =
        Snapshot.of(serializationFormat.encodeToString(serializationStrategy, value))

    internal companion object {
        internal val json = Json {
            serializersModule = ChartModel.serializerModule
        }
    }
}

public abstract class StatefulWorkflowWithSnapshots<PropsT, StateT : Any, OutputT, RenderingT> :
    StatefulWorkflow<PropsT, StateT, OutputT, RenderingT>() {

    protected abstract val stateSnapshotSerializer: StateSnapshotSerializer<StateT>

    protected abstract fun initialState(props: PropsT): StateT

    override fun initialState(props: PropsT, snapshot: Snapshot?): StateT =
        snapshot?.let { stateSnapshotSerializer.fromSnapshot(it) }
            ?: initialState(props)

    override fun snapshotState(state: StateT): Snapshot = stateSnapshotSerializer.toSnapshot(state)
}

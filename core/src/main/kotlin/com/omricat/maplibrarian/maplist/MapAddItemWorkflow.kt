package com.omricat.maplibrarian.maplist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.maplist.MapAddItemWorkflow.EditingMapModel
import com.omricat.maplibrarian.maplist.MapAddItemWorkflow.Event
import com.omricat.maplibrarian.maplist.MapAddItemWorkflow.Event.Discard
import com.omricat.maplibrarian.maplist.MapAddItemWorkflow.Event.Saved
import com.omricat.maplibrarian.maplist.MapAddItemWorkflow.State
import com.omricat.maplibrarian.maplist.MapAddItemWorkflow.State.Editing
import com.omricat.maplibrarian.maplist.MapAddItemWorkflow.State.Saving
import com.omricat.maplibrarian.model.DbMapModel
import com.omricat.maplibrarian.model.MapModel
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.UserUid
import com.omricat.workflow.eventHandler
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker

public class MapAddItemWorkflow(private val mapsService: MapsService) :
    StatefulWorkflow<User, State, Event, AddingItemScreen>() {

    public data class EditingMapModel(
        override val title: String,
        override val userId: UserUid
    ) : MapModel

    public sealed interface State {
        public data class Editing(val map: EditingMapModel, val errorMessage: String = "") : State

        public data class Saving(val map: MapModel) : State
    }

    public sealed interface Event {
        public object Discard : Event
        public object Saved : Event
    }

    override fun initialState(props: User, snapshot: Snapshot?): State =
        Editing(EditingMapModel("", props.id))

    override fun render(
        renderProps: User,
        renderState: State,
        context: RenderContext
    ): AddingItemScreen = when (renderState) {
        is Editing -> {
            AddItemScreen(
                map = renderState.map,
                errorMessage = renderState.errorMessage,
                onTitleChanged = context.eventHandler(onTitleChanged(renderState)),
                discardChanges = context.eventHandler(::onDiscard),
                saveChanges = context.eventHandler(onSave(renderState.map))
            )
        }
        is Saving -> {
            context.runningWorker(saveNewItem(renderProps, renderState.map)) { result ->
                result.map { savedMap -> onNewItemSaved() }
                    .getOrElse { e ->
                        action {
                            state = Editing(
                                map = editingMap(renderState.map),
                                errorMessage = e.message
                            )
                        }
                    }
            }
            SavingItemScreen(renderState.map)
        }
    }

    internal fun onNewItemSaved() = action { setOutput(Saved) }

    public override fun snapshotState(state: State): Snapshot? = null // TODO: Implement snapshots

    private fun saveNewItem(
        user: User,
        map: MapModel
    ): Worker<Result<DbMapModel, MapsServiceError>> =
        resultWorker(MapsServiceError::fromThrowable) { mapsService.addNewMap(user, map) }

    private fun onSave(map: MapModel) = action { state = Saving(map) }

    private fun onDiscard() = action { setOutput(Discard) }

    private fun onTitleChanged(editingState: Editing) = { newTitle: CharSequence ->
        action { state = Editing(map = editingState.map.withTitle(newTitle.toString())) }
    }
}

public sealed interface AddingItemScreen : MapsScreen {
    public val map: MapModel
}

public data class AddItemScreen(
    override val map: MapModel,
    val errorMessage: String = "",
    val onTitleChanged: (CharSequence) -> Unit,
    val discardChanges: () -> Unit,
    val saveChanges: () -> Unit
) : AddingItemScreen

public data class SavingItemScreen(override val map: MapModel) : AddingItemScreen

private fun EditingMapModel.withTitle(newTitle: String) = copy(title = newTitle)

private fun editingMap(map: MapModel) =
    EditingMapModel(title = map.title.toString(), userId = map.userId)

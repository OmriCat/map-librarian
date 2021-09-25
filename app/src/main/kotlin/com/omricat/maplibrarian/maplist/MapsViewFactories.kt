@file:OptIn(WorkflowUiExperimentalApi::class)

package com.omricat.maplibrarian.maplist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omricat.maplibrarian.databinding.ErrorBinding
import com.omricat.maplibrarian.databinding.MaplistBinding
import com.omricat.maplibrarian.databinding.MaplistItemBinding
import com.omricat.maplibrarian.databinding.MaplistLoadingBinding
import com.omricat.maplibrarian.maplist.MapListAdapter.MapViewHolder
import com.omricat.maplibrarian.model.DbMapModel
import com.squareup.workflow1.ui.LayoutRunner
import com.squareup.workflow1.ui.LayoutRunner.Companion.bind
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import timber.log.Timber

internal val MapListLoadingViewFactory: ViewFactory<MapsScreen.Loading> =
    bind(MaplistLoadingBinding::inflate) { _, _ -> }

internal class MapListLayoutRunner(private val binding: MaplistBinding) :
    LayoutRunner<MapListScreen> {
    private val adapter: MapListAdapter = MapListAdapter()

    init {
        with(binding) {
            mapList.adapter = adapter
            mapList.layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun showRendering(rendering: MapListScreen, viewEnvironment: ViewEnvironment) {
        adapter.submitList(rendering.list)
        adapter.onClick = rendering.onItemSelect
        adapter.notifyDataSetChanged()
    }

    companion object : ViewFactory<MapListScreen> by bind(
        MaplistBinding::inflate, ::MapListLayoutRunner
    )
}

internal val MapsErrorViewFactory: ViewFactory<MapsScreen.ShowError> =
    bind(ErrorBinding::inflate) { error, _ ->
        errorMessage.text = error.message
    }

internal class MapListAdapter : ListAdapter<DbMapModel, MapViewHolder>(MapDiffCallback) {
    object MapDiffCallback : DiffUtil.ItemCallback<DbMapModel>() {
        override fun areItemsTheSame(oldItem: DbMapModel, newItem: DbMapModel): Boolean =
            oldItem.mapId == newItem.mapId

        override fun areContentsTheSame(oldItem: DbMapModel, newItem: DbMapModel): Boolean = oldItem == newItem
    }

    internal var onClick: (Int) -> Unit = {}

    class MapViewHolder(
        internal val binding: MaplistItemBinding,
        internal val onClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapViewHolder =
        MapViewHolder(
            MaplistItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onClick
        )

    override fun onBindViewHolder(holder: MapViewHolder, position: Int) {
        with(holder.binding) {
            mapItemTitle.text = getItem(position).title
            root.setOnClickListener {
                holder.onClick(position)
                Timber.d("Item clicked at position %d", position)
            }
        }
    }
}

internal val MapListViewRegistry = ViewRegistry(
    MapListLoadingViewFactory,
    MapsErrorViewFactory,
    MapListLayoutRunner
)

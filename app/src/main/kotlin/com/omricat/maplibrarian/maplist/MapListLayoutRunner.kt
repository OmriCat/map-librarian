package com.omricat.maplibrarian.maplist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omricat.maplibrarian.databinding.MaplistBinding
import com.omricat.maplibrarian.databinding.MaplistItemBinding
import com.omricat.maplibrarian.maplist.MapListAdapter.MapViewHolder
import com.omricat.maplibrarian.model.DbMapModel
import com.squareup.workflow1.ui.LayoutRunner
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import timber.log.Timber

@OptIn(WorkflowUiExperimentalApi::class)
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
    }

    companion object : ViewFactory<MapListScreen> by LayoutRunner.bind(
        MaplistBinding::inflate, ::MapListLayoutRunner
    )
}

internal class MapListAdapter : ListAdapter<DbMapModel, MapViewHolder>(MapDiffCallback) {
    internal var onClick: (Int) -> Unit = {}

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

    class MapViewHolder(
        internal val binding: MaplistItemBinding,
        internal val onClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root)

    private object MapDiffCallback : DiffUtil.ItemCallback<DbMapModel>() {
        override fun areItemsTheSame(oldItem: DbMapModel, newItem: DbMapModel): Boolean =
            oldItem.mapId == newItem.mapId

        override fun areContentsTheSame(oldItem: DbMapModel, newItem: DbMapModel): Boolean = oldItem == newItem
    }
}

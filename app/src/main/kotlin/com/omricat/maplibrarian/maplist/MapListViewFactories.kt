@file:OptIn(WorkflowUiExperimentalApi::class)

package com.omricat.maplibrarian.maplist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omricat.maplibrarian.model.Map
import com.omricat.maplibrarian.databinding.LayoutMaplistBinding
import com.omricat.maplibrarian.databinding.LayoutMaplistErrorBinding
import com.omricat.maplibrarian.databinding.LayoutMaplistLoadingBinding
import com.omricat.maplibrarian.databinding.MaplistItemBinding
import com.omricat.maplibrarian.maplist.MapListAdapter.MapViewHolder
import com.omricat.maplibrarian.maplist.MapListScreen.MapList
import com.squareup.workflow1.ui.LayoutRunner
import com.squareup.workflow1.ui.LayoutRunner.Companion.bind
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

internal val MapListLoadingViewFactory: ViewFactory<MapListScreen.Loading> =
    bind(LayoutMaplistLoadingBinding::inflate) { _, _ -> }

internal class MapListLayoutRunner(private val binding: LayoutMaplistBinding) :
    LayoutRunner<MapList> {
    private val adapter: MapListAdapter = MapListAdapter()

    private val logOutMenuItem = binding.toolbar.menu.add("Log out").apply {
        setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
    }

    init {
        with(binding) {
            mapList.adapter = adapter
            mapList.layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun showRendering(rendering: MapList, viewEnvironment: ViewEnvironment) {
        logOutMenuItem.setOnMenuItemClickListener { rendering.logOutCmd(); true }
        adapter.list = rendering.list
        adapter.notifyDataSetChanged()
    }

    companion object : ViewFactory<MapList> by bind(
        LayoutMaplistBinding::inflate, ::MapListLayoutRunner
    )
}

internal val MapListErrorViewFactory: ViewFactory<MapListScreen.ShowError> =
    bind(LayoutMaplistErrorBinding::inflate) { error, _ ->
        maplistErrorMessage.text = error.message
    }

internal class MapListAdapter : RecyclerView.Adapter<MapViewHolder>() {
    internal var list: List<Map> = emptyList()

    class MapViewHolder(internal val binding: MaplistItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapViewHolder =
        MapViewHolder(
            MaplistItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: MapViewHolder, position: Int) {
        with(holder.binding) {
            mapItemTitle.text = list[position].title
        }
    }

    override fun getItemCount(): Int = list.size
}

internal val MapListViewRegistry = ViewRegistry(
    MapListLoadingViewFactory,
    MapListErrorViewFactory,
    MapListLayoutRunner
)

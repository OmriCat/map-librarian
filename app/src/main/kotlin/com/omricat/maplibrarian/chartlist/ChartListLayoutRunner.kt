package com.omricat.maplibrarian.chartlist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omricat.maplibrarian.chartlist.ChartListAdapter.ChartViewHolder
import com.omricat.maplibrarian.databinding.ChartlistBinding
import com.omricat.maplibrarian.databinding.ChartlistItemBinding
import com.omricat.maplibrarian.model.DbChartModel
import com.squareup.workflow1.ui.LayoutRunner
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import timber.log.Timber

@OptIn(WorkflowUiExperimentalApi::class)
internal class ChartListLayoutRunner(private val binding: ChartlistBinding) :
    LayoutRunner<ChartListScreen> {
    private val adapter: ChartListAdapter = ChartListAdapter()

    init {
        with(binding) {
            chartList.adapter = adapter
            chartList.layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun showRendering(rendering: ChartListScreen, viewEnvironment: ViewEnvironment) {
        adapter.submitList(rendering.list)
        adapter.onClick = rendering.onItemSelect
    }

    companion object : ViewFactory<ChartListScreen> by LayoutRunner.bind(
        ChartlistBinding::inflate,
        ::ChartListLayoutRunner
    )
}

internal class ChartListAdapter : ListAdapter<DbChartModel, ChartViewHolder>(ChartDiffCallback) {
    internal var onClick: (Int) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder =
        ChartViewHolder(
            ChartlistItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onClick
        )

    override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {
        with(holder.binding) {
            chartItemTitle.text = getItem(position).title
            root.setOnClickListener {
                holder.onClick(position)
                Timber.d("Item clicked at position %d", position)
            }
        }
    }

    class ChartViewHolder(
        internal val binding: ChartlistItemBinding,
        internal val onClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root)

    private object ChartDiffCallback : DiffUtil.ItemCallback<DbChartModel>() {
        override fun areItemsTheSame(oldItem: DbChartModel, newItem: DbChartModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DbChartModel, newItem: DbChartModel): Boolean =
            oldItem == newItem
    }
}

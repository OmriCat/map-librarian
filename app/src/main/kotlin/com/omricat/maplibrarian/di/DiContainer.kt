package com.omricat.maplibrarian.di

import com.omricat.logging.Logger
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.auth.UserRepository
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow
import com.omricat.maplibrarian.chartlist.ChartDetailsWorkflow
import com.omricat.maplibrarian.chartlist.ChartsRepository
import com.omricat.maplibrarian.chartlist.ChartsWorkflow
import com.omricat.maplibrarian.root.RootWorkflow
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import kotlinx.serialization.StringFormat

@OptIn(WorkflowUiExperimentalApi::class)
interface DiContainer {
    val userRepository: UserRepository
    val chartsRepository: ChartsRepository
    val workflows: Workflows
    val viewRegistry: ViewRegistry
    val logger: Logger
    val stringFormat: StringFormat

    interface Workflows {
        val root: RootWorkflow
        val auth: AuthWorkflow
        val charts: ChartsWorkflow
        val addNewChart: AddNewChartWorkflow
        val chartDetails: ChartDetailsWorkflow
    }
}

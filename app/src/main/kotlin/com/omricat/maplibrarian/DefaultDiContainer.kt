package com.omricat.maplibrarian

import com.omricat.maplibrarian.auth.ActualAuthWorkflow
import com.omricat.maplibrarian.auth.AuthViewRegistry
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.auth.SignUpWorkflow
import com.omricat.maplibrarian.chartlist.ActualChartsWorkflow
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow
import com.omricat.maplibrarian.chartlist.ChartsWorkflow
import com.omricat.maplibrarian.chartlist.MapListViewRegistry
import com.omricat.maplibrarian.root.AuthorizedScreenLayoutRunner
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.plus

@WorkflowUiExperimentalApi
abstract class DefaultDiContainer : DiContainer {

    override val workflows: DiContainer.Workflows =
        object : DiContainer.Workflows {
            override val auth: AuthWorkflow by lazy {
                ActualAuthWorkflow(userRepository, SignUpWorkflow.instance(userRepository))
            }

            override val charts: ChartsWorkflow by lazy {
                ActualChartsWorkflow(chartsRepository, AddNewChartWorkflow(chartsRepository))
            }
        }
    override val viewRegistry: ViewRegistry =
        ViewRegistry(AuthorizedScreenLayoutRunner) + AuthViewRegistry + MapListViewRegistry
}

package com.omricat.maplibrarian

import com.omricat.logging.Logger
import com.omricat.maplibrarian.auth.AuthViewRegistry
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.auth.SignUpWorkflow
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow
import com.omricat.maplibrarian.chartlist.ChartDetailsWorkflow
import com.omricat.maplibrarian.chartlist.ChartsWorkflow
import com.omricat.maplibrarian.chartlist.MapListViewRegistry
import com.omricat.maplibrarian.di.DiContainer
import com.omricat.maplibrarian.root.AuthorizedScreenLayoutRunner
import com.omricat.maplibrarian.root.RootWorkflow
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.plus
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json

@WorkflowUiExperimentalApi
abstract class DefaultDiContainer : DiContainer {

    override val logger: Logger by lazy { Logger.NoOpLogger }

    override val stringFormat: StringFormat = Json

    override val workflows: DiContainer.Workflows =
        object : DiContainer.Workflows {

            override val root: RootWorkflow by lazy {
                RootWorkflow.instance(
                    userRepository,
                    auth,
                    charts,
                    logger,
                )
            }

            override val auth: AuthWorkflow by lazy {
                AuthWorkflow.instance(userRepository, SignUpWorkflow.instance(userRepository))
            }

            override val addNewChart by lazy {
                AddNewChartWorkflow.instance(chartsRepository, stringFormat)
            }

            override val chartDetails by lazy {
                ChartDetailsWorkflow.instance(
                    addNewChart,
                    stringFormat,
                )
            }
            override val charts: ChartsWorkflow by lazy {
                ChartsWorkflow.instance(
                    chartsRepository,
                    addNewChart,
                    chartDetails,
                    stringFormat,
                )
            }
        }
    override val viewRegistry: ViewRegistry =
        ViewRegistry(AuthorizedScreenLayoutRunner) + AuthViewRegistry + MapListViewRegistry
}

package com.omricat.maplibrarian

import com.omricat.maplibrarian.auth.ActualAuthWorkflow
import com.omricat.maplibrarian.auth.AuthViewRegistry
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.auth.SignUpWorkflow
import com.omricat.maplibrarian.maplist.ActualMapsWorkflow
import com.omricat.maplibrarian.maplist.MapAddItemWorkflow
import com.omricat.maplibrarian.maplist.MapListViewRegistry
import com.omricat.maplibrarian.maplist.MapsWorkflow
import com.omricat.maplibrarian.root.AuthorizedScreenLayoutRunner
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.plus

@WorkflowUiExperimentalApi
abstract class DefaultDiContainer : MapLibDiContainer {

    override val workflows: MapLibDiContainer.Workflows = object : MapLibDiContainer.Workflows {
        override val auth: AuthWorkflow by lazy {
            ActualAuthWorkflow(
                authService,
                SignUpWorkflow.instance(authService)
            )
        }

        override val maps: MapsWorkflow by lazy {
            ActualMapsWorkflow(
                mapsService,
                MapAddItemWorkflow(mapsService)
            )
        }
    }
    override val viewRegistry: ViewRegistry =
        ViewRegistry(AuthorizedScreenLayoutRunner) +
                AuthViewRegistry + MapListViewRegistry
}

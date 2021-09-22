package com.omricat.maplibrarian.root

import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.root.AuthorizedWorkflow.Output
import com.squareup.workflow1.StatelessWorkflow
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.renderChild

internal class AuthorizedWorkflow<ChildPropsT : AuthorizedProps, ChildRenderingT : Screen>(
    private val childWorkflow: Workflow<ChildPropsT, Nothing, ChildRenderingT>
) : StatelessWorkflow<ChildPropsT, Output, AuthorizedScreen<ChildRenderingT>>() {

    internal sealed interface Output {
        object LogOut : Output
    }

    override fun render(
        renderProps: ChildPropsT,
        context: RenderContext
    ): AuthorizedScreen<ChildRenderingT> {
        val childRendering = context.renderChild(childWorkflow, renderProps)
        return AuthorizedScreen(
            onLogoutClicked = context.eventHandler { setOutput(Output.LogOut) },
            subScreen = childRendering
        )
    }
}

internal interface AuthorizedProps {
    val user: User
}

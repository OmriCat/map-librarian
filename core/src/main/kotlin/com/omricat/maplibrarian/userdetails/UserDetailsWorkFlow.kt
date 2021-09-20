package com.omricat.maplibrarian.userdetails

import com.omricat.maplibrarian.model.User
import com.squareup.workflow1.StatelessWorkflow

public object UserDetailsWorkFlow :
    StatelessWorkflow<User, UserDetailsWorkFlow.LogOut, UserDetailsScreen>() {
    public object LogOut

    override fun render(renderProps: User, context: RenderContext): UserDetailsScreen =
        UserDetailsScreen(
            user = renderProps,
            onLogOutClicked = context.eventHandler { setOutput(LogOut) }
        )
}

public data class UserDetailsScreen(
    val user: User,
    val onLogOutClicked: () -> Unit
)

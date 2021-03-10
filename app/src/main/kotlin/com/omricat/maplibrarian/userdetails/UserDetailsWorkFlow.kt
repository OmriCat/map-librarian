package com.omricat.maplibrarian.userdetails

import com.omricat.maplibrarian.auth.User
import com.squareup.workflow1.StatelessWorkflow

object UserDetailsWorkFlow :
    StatelessWorkflow<User, UserDetailsWorkFlow.LogOut, UserDetailsScreen>() {
    object LogOut

    override fun render(props: User, context: RenderContext): UserDetailsScreen =
        UserDetailsScreen(
            user = props,
            onLogOutClicked = context.eventHandler { setOutput(LogOut) }
        )
}

data class UserDetailsScreen(
    val user: User,
    val onLogOutClicked: () -> Unit
)

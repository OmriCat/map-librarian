package com.omricat.maplibrarian.auth

import com.omricat.maplibrarian.model.EmailAddress
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.UserUid

internal data class TestUser(
    override val displayName: String,
    override val id: UserUid,
    override val emailAddress: EmailAddress
) : User {
    companion object {
        operator fun invoke(displayName: String, id: UserUid, emailAddress: String): TestUser =
            TestUser(displayName, id, EmailAddress(emailAddress))
    }
}

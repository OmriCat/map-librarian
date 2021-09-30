package com.omricat.maplibrarian.auth

import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.UserUid

internal data class TestUser(override val displayName: String, override val id: UserUid) : User

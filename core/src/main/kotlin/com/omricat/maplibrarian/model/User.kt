package com.omricat.maplibrarian.model

import kotlinx.serialization.Serializable

@Serializable @JvmInline public value class UserUid(public val id: String)

public interface User {
    public val displayName: String
    public val id: UserUid

    public companion object
}

package com.omricat.maplibrarian.model

import kotlinx.serialization.Serializable

@Serializable @JvmInline public value class UserUid(public val value: String)

@Serializable @JvmInline public value class EmailAddress(public val value: String)

public interface User {
    public val displayName: String
    public val id: UserUid
    public val emailAddress: EmailAddress

    public companion object
}

package com.omricat.maplibrarian.auth

public sealed interface Credential
public data class EmailPasswordCredential(
    val emailAddress: String,
    val password: String
) : Credential

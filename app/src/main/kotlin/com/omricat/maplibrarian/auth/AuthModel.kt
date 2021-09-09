package com.omricat.maplibrarian.auth

sealed interface Credential
data class EmailPasswordCredential(val emailAddress: String, val password: String) : Credential

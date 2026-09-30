package com.dodo.proxytoggle.domain.model

data class ProxyPreferences(
    val host: String,
    val port: String,
    val darkMode: Boolean
)

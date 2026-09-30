package com.dodo.proxytoggle.domain.model

import java.util.Locale

/**
 * A proxy the user has successfully connected to before.
 */
data class SavedProxy(val host: String, val port: Int) {
    val label: String get() = "$host:$port"

    fun sameAs(other: SavedProxy) =
        port == other.port && host.lowercase(Locale.ROOT) == other.host.lowercase(Locale.ROOT)
}

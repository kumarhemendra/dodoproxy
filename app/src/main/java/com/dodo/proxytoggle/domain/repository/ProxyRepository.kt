package com.dodo.proxytoggle.domain.repository

import com.dodo.proxytoggle.domain.model.ProxyPreferences
import com.dodo.proxytoggle.domain.model.SavedProxy

interface ProxyRepository {
    fun loadPreferences(): ProxyPreferences
    fun saveHostAndPort(host: String, port: String)
    fun saveDarkMode(enabled: Boolean)
    fun loadProxyHistory(): List<SavedProxy>
    fun addToProxyHistory(proxy: SavedProxy)
    fun removeFromProxyHistory(proxy: SavedProxy)
    fun currentProxy(): String?
    fun isProxyConfigured(host: String, port: String): Boolean
    fun connect(host: String, port: Int): ProxyOperationResult
    fun disconnect(host: String, port: String): ProxyOperationResult
}

enum class ProxyOperationResult {
    SUCCESS,
    PERMISSION_REQUIRED,
    APPLY_FAILED,
    DISABLE_FAILED
}

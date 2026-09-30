package com.dodo.proxytoggle.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.dodo.proxytoggle.R
import com.dodo.proxytoggle.domain.model.ProxyPreferences
import com.dodo.proxytoggle.domain.model.SavedProxy
import com.dodo.proxytoggle.domain.repository.ProxyRepository
import com.dodo.proxytoggle.domain.usecase.ConnectProxyResult
import com.dodo.proxytoggle.domain.usecase.ConnectProxyUseCase
import com.dodo.proxytoggle.domain.usecase.DisconnectProxyResult
import com.dodo.proxytoggle.domain.usecase.DisconnectProxyUseCase

data class ProxyUiState(
    val host: String,
    val port: String,
    val darkMode: Boolean,
    val connected: Boolean = false,
    val currentProxy: String? = null,
    val hostInvalid: Boolean = false,
    val portInvalid: Boolean = false,
    val dialog: ProxyDialog? = null,
    val message: ProxyMessage? = null,
    val history: List<SavedProxy> = emptyList()
)

enum class ProxyDialog { PERMISSION, INFO, STATUS }

data class ProxyMessage(@param:StringRes val text: Int, val long: Boolean = false)

class ProxyViewModel(private val repository: ProxyRepository) : ViewModel() {
    private val connectProxy = ConnectProxyUseCase(repository)
    private val disconnectProxy = DisconnectProxyUseCase(repository)

    var state by mutableStateOf(repository.loadPreferences().toUiState())
        private set

    fun onHostChanged(value: String) {
        repository.saveHostAndPort(value, state.port)
        state = state.copy(host = value, hostInvalid = false)
    }

    fun onPortChanged(value: String) {
        repository.saveHostAndPort(state.host, value)
        state = state.copy(port = value, portInvalid = false)
    }

    fun selectSavedProxy(proxy: SavedProxy) {
        val port = proxy.port.toString()
        repository.saveHostAndPort(proxy.host, port)
        state = state.copy(host = proxy.host, port = port, hostInvalid = false, portInvalid = false)
    }

    fun removeSavedProxy(proxy: SavedProxy) {
        repository.removeFromProxyHistory(proxy)
        state = state.copy(history = repository.loadProxyHistory())
    }

    fun toggleDarkMode() {
        val enabled = !state.darkMode
        repository.saveDarkMode(enabled)
        state = state.copy(darkMode = enabled)
    }

    fun toggleProxy() {
        if (state.connected) disconnect() else connect()
    }

    fun showDialog(dialog: ProxyDialog) {
        state = state.copy(dialog = dialog)
    }

    fun dismissDialog() {
        state = state.copy(dialog = null)
    }

    fun clearMessage() {
        state = state.copy(message = null)
    }

    fun refresh() {
        val preferences = repository.loadPreferences()
        state = state.copy(
            host = preferences.host,
            port = preferences.port,
            darkMode = preferences.darkMode,
            connected = repository.isProxyConfigured(preferences.host, preferences.port),
            currentProxy = repository.currentProxy(),
            history = repository.loadProxyHistory()
        )
    }

    private fun connect() {
        when (connectProxy.execute(state.host, state.port)) {
            ConnectProxyResult.Connected -> {
                state = state.copy(
                    connected = true,
                    currentProxy = "${state.host.trim()}:${state.port.trim()}",
                    hostInvalid = false,
                    portInvalid = false,
                    message = ProxyMessage(R.string.proxy_enabled),
                    history = repository.loadProxyHistory()
                )
            }
            ConnectProxyResult.InvalidHost -> state = state.copy(hostInvalid = true)
            ConnectProxyResult.InvalidPort -> state = state.copy(portInvalid = true)
            ConnectProxyResult.PermissionRequired -> showDialog(ProxyDialog.PERMISSION)
            ConnectProxyResult.Failed -> state = state.copy(
                message = ProxyMessage(R.string.proxy_apply_failed, long = true)
            )
        }
    }

    private fun disconnect() {
        when (disconnectProxy.execute(state.host, state.port)) {
            DisconnectProxyResult.Disconnected -> {
                state = state.copy(
                    connected = false,
                    currentProxy = repository.currentProxy(),
                    message = ProxyMessage(R.string.proxy_disabled)
                )
            }
            DisconnectProxyResult.PermissionRequired -> showDialog(ProxyDialog.PERMISSION)
            DisconnectProxyResult.Failed -> state = state.copy(
                message = ProxyMessage(R.string.proxy_disable_failed, long = true)
            )
        }
    }

    private fun ProxyPreferences.toUiState() = ProxyUiState(
        host = host,
        port = port,
        darkMode = darkMode,
        connected = repository.isProxyConfigured(host, port),
        currentProxy = repository.currentProxy(),
        history = repository.loadProxyHistory()
    )
}

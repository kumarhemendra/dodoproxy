package com.dodo.proxytoggle.domain.usecase

import com.dodo.proxytoggle.domain.model.SavedProxy
import com.dodo.proxytoggle.domain.repository.ProxyOperationResult
import com.dodo.proxytoggle.domain.repository.ProxyRepository

class ConnectProxyUseCase(private val repository: ProxyRepository) {
    fun execute(host: String, port: String): ConnectProxyResult {
        val cleanHost = host.trim()
        if (cleanHost.isEmpty() || cleanHost.any { it.isWhitespace() || it == ':' || it == '/' }) {
            return ConnectProxyResult.InvalidHost
        }

        val cleanPort = port.trim().toIntOrNull()
        if (cleanPort == null || cleanPort !in 1..65535) {
            return ConnectProxyResult.InvalidPort
        }

        repository.saveHostAndPort(cleanHost, cleanPort.toString())
        return when (repository.connect(cleanHost, cleanPort)) {
            ProxyOperationResult.SUCCESS -> {
                repository.addToProxyHistory(SavedProxy(cleanHost, cleanPort))
                ConnectProxyResult.Connected
            }
            ProxyOperationResult.PERMISSION_REQUIRED -> ConnectProxyResult.PermissionRequired
            ProxyOperationResult.APPLY_FAILED,
            ProxyOperationResult.DISABLE_FAILED -> ConnectProxyResult.Failed
        }
    }
}

sealed interface ConnectProxyResult {
    data object Connected : ConnectProxyResult
    data object InvalidHost : ConnectProxyResult
    data object InvalidPort : ConnectProxyResult
    data object PermissionRequired : ConnectProxyResult
    data object Failed : ConnectProxyResult
}

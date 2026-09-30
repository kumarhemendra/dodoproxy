package com.dodo.proxytoggle.domain.usecase

import com.dodo.proxytoggle.domain.repository.ProxyOperationResult
import com.dodo.proxytoggle.domain.repository.ProxyRepository

class DisconnectProxyUseCase(private val repository: ProxyRepository) {
    fun execute(host: String, port: String): DisconnectProxyResult =
        when (repository.disconnect(host.trim(), port.trim())) {
            ProxyOperationResult.SUCCESS -> DisconnectProxyResult.Disconnected
            ProxyOperationResult.PERMISSION_REQUIRED -> DisconnectProxyResult.PermissionRequired
            ProxyOperationResult.DISABLE_FAILED,
            ProxyOperationResult.APPLY_FAILED -> DisconnectProxyResult.Failed
        }
}

sealed interface DisconnectProxyResult {
    data object Disconnected : DisconnectProxyResult
    data object PermissionRequired : DisconnectProxyResult
    data object Failed : DisconnectProxyResult
}

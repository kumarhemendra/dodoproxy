package com.dodo.proxytoggle.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.edit
import com.dodo.proxytoggle.domain.model.ProxyPreferences
import com.dodo.proxytoggle.domain.model.SavedProxy
import com.dodo.proxytoggle.domain.repository.ProxyOperationResult
import com.dodo.proxytoggle.domain.repository.ProxyRepository
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class AndroidProxyRepository(context: Context) : ProxyRepository {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences("proxy_toggle", Context.MODE_PRIVATE)

    override fun loadPreferences() = ProxyPreferences(
        host = preferences.getString(KEY_HOST, DEFAULT_HOST) ?: DEFAULT_HOST,
        port = preferences.getString(KEY_PORT, DEFAULT_PORT) ?: DEFAULT_PORT,
        darkMode = preferences.getBoolean(KEY_DARK_MODE, true)
    )

    override fun saveHostAndPort(host: String, port: String) {
        preferences.edit {
            putString(KEY_HOST, host)
            putString(KEY_PORT, port)
        }
    }

    override fun saveDarkMode(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_DARK_MODE, enabled) }
    }

    override fun loadProxyHistory(): List<SavedProxy> {
        val raw = preferences.getString(KEY_PROXY_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val host = item.optString(JSON_HOST).takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val port = item.optInt(JSON_PORT, -1).takeIf { it in 1..65535 } ?: return@mapNotNull null
                SavedProxy(host, port)
            }
        } catch (_: JSONException) {
            emptyList()
        }
    }

    override fun addToProxyHistory(proxy: SavedProxy) {
        val updated = listOf(proxy) + loadProxyHistory().filterNot { it.sameAs(proxy) }
        saveProxyHistory(updated.take(MAX_PROXY_HISTORY))
    }

    override fun removeFromProxyHistory(proxy: SavedProxy) {
        saveProxyHistory(loadProxyHistory().filterNot { it.sameAs(proxy) })
    }

    private fun saveProxyHistory(history: List<SavedProxy>) {
        val array = JSONArray()
        history.forEach { array.put(JSONObject().put(JSON_HOST, it.host).put(JSON_PORT, it.port)) }
        preferences.edit { putString(KEY_PROXY_HISTORY, array.toString()) }
    }

    override fun currentProxy(): String? =
        Settings.Global.getString(appContext.contentResolver, Settings.Global.HTTP_PROXY)

    override fun isProxyConfigured(host: String, port: String): Boolean {
        val expected = preferences.getString(KEY_APPLIED_PROXY, null)
            ?: if (host.isBlank() || port.toIntOrNull() == null) return false else "$host:$port"
        return sameProxy(currentProxy(), expected)
    }

    override fun connect(host: String, port: Int): ProxyOperationResult {
        if (!hasPermission()) return ProxyOperationResult.PERMISSION_REQUIRED
        val proxy = "$host:$port"
        val current = currentProxy()

        return try {
            if (!sameProxy(current, proxy)) {
                preferences.edit {
                    putString(KEY_PREVIOUS_PROXY, current.orEmpty())
                    putBoolean(KEY_PREVIOUS_PROXY_WAS_SET, current != null)
                }
            } else {
                preferences.edit {
                    remove(KEY_PREVIOUS_PROXY)
                    putBoolean(KEY_PREVIOUS_PROXY_WAS_SET, false)
                }
            }
            if (!sameProxy(current, proxy) &&
                !Settings.Global.putString(appContext.contentResolver, Settings.Global.HTTP_PROXY, proxy)
            ) {
                preferences.edit {
                    remove(KEY_PREVIOUS_PROXY)
                    remove(KEY_PREVIOUS_PROXY_WAS_SET)
                }
                return ProxyOperationResult.APPLY_FAILED
            }
            preferences.edit {
                putString(KEY_APPLIED_PROXY, proxy)
            }
            ProxyOperationResult.SUCCESS
        } catch (_: SecurityException) {
            ProxyOperationResult.PERMISSION_REQUIRED
        }
    }

    override fun disconnect(host: String, port: String): ProxyOperationResult {
        if (!hasPermission()) return ProxyOperationResult.PERMISSION_REQUIRED
        val configured = preferences.getString(KEY_APPLIED_PROXY, null) ?: "$host:$port"
        val current = currentProxy()

        return try {
            if (sameProxy(current, configured)) {
                val hadPrevious = preferences.getBoolean(KEY_PREVIOUS_PROXY_WAS_SET, false)
                val previous = preferences.getString(KEY_PREVIOUS_PROXY, null)
                if (!Settings.Global.putString(
                        appContext.contentResolver,
                        Settings.Global.HTTP_PROXY,
                        if (hadPrevious) previous else null
                    )
                ) {
                    return ProxyOperationResult.DISABLE_FAILED
                }
            }
            preferences.edit {
                remove(KEY_APPLIED_PROXY)
                remove(KEY_PREVIOUS_PROXY)
                remove(KEY_PREVIOUS_PROXY_WAS_SET)
            }
            ProxyOperationResult.SUCCESS
        } catch (_: SecurityException) {
            ProxyOperationResult.PERMISSION_REQUIRED
        }
    }

    private fun hasPermission() =
        appContext.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) ==
            PackageManager.PERMISSION_GRANTED

    private fun sameProxy(first: String?, second: String): Boolean {
        if (first == null) return false
        fun normalize(value: String): String {
            val parts = value.trim().split(':', limit = 2)
            return if (parts.size == 2) {
                "${parts[0].lowercase(Locale.ROOT)}:${parts[1]}"
            } else {
                value.trim().lowercase(Locale.ROOT)
            }
        }
        return normalize(first) == normalize(second)
    }

    private companion object {
        const val KEY_HOST = "host"
        const val KEY_PORT = "port"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_APPLIED_PROXY = "applied_proxy"
        const val KEY_PREVIOUS_PROXY = "previous_proxy"
        const val KEY_PREVIOUS_PROXY_WAS_SET = "previous_proxy_was_set"
        const val KEY_PROXY_HISTORY = "proxy_history"
        const val JSON_HOST = "host"
        const val JSON_PORT = "port"
        const val MAX_PROXY_HISTORY = 10
        const val DEFAULT_HOST = "192.168.1.2"
        const val DEFAULT_PORT = "8888"
    }
}

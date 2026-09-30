package com.dodo.proxytoggle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dodo.proxytoggle.data.repository.AndroidProxyRepository
import com.dodo.proxytoggle.presentation.ProxyScreen
import com.dodo.proxytoggle.presentation.ProxyViewModel

class MainActivity : ComponentActivity() {
    private val proxyViewModel: ProxyViewModel by lazy {
        ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (!modelClass.isAssignableFrom(ProxyViewModel::class.java)) {
                        throw IllegalArgumentException("Unsupported ViewModel: $modelClass")
                    }
                    return ProxyViewModel(AndroidProxyRepository(applicationContext)) as T
                }
            }
        )[ProxyViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ProxyScreen(proxyViewModel) { darkMode ->
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkMode
                    isAppearanceLightNavigationBars = !darkMode
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        proxyViewModel.refresh()
    }
}

package com.example.linkup.utils

import android.content.Context
import android.util.Log
import com.example.linkup.monitorNetwork.ConnectionStatus
import com.example.linkup.monitorNetwork.observeConnectivityAsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun observeNetworkStatus(context: Context, coroutineScope: CoroutineScope, showToast: (String, String, Boolean) -> Unit) {
    coroutineScope.launch {
        context.observeConnectivityAsFlow().collect { status ->
            when (status) {
                is ConnectionStatus.Available -> {
                    Log.d("NetworkStatus", "Internet available")
                    showToast("Internet Status", "Internet is available", true)
                }
                is ConnectionStatus.Unavailable -> {
                    Log.d("NetworkStatus", "Internet unavailable")
                    showToast("Internet Status", "Internet is unavailable", false)
                }
            }
        }
    }
}

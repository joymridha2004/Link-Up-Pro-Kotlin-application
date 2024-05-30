package com.example.linkup.monitorNetwork

sealed class ConnectionStatus {
    object Available : ConnectionStatus()
    object Unavailable : ConnectionStatus()
}
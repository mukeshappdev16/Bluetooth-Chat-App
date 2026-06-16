package com.ms.bluetoothchatapp.domain.chat

sealed interface ConnectionResult {
    data object ConnectionEstablished : ConnectionResult
    data class Error(val message: String) : ConnectionResult
}

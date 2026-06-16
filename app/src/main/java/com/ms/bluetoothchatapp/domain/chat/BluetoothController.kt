package com.ms.bluetoothchatapp.domain.chat

import com.ms.bluetoothchatapp.domain.model.BluetoothDeviceUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val isConnected: StateFlow<Boolean>
    val isDiscovering: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDeviceUI>>
    val pairedDevices: StateFlow<List<BluetoothDeviceUI>>
    val errors: SharedFlow<String>
    val messages: StateFlow<List<BluetoothMessage>>

    fun startDiscovery()
    fun stopDiscovery()

    fun startBluetoothServer(): Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDeviceUI): Flow<ConnectionResult>

    suspend fun trySendMessage(message: String): BluetoothMessage?

    fun closeConnection()
    fun release()
}

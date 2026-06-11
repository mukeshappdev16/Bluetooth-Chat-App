package com.ms.bluetoothchatapp.domain.discovery

import com.ms.bluetoothchatapp.domain.model.BluetoothDeviceUI
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface DiscoveryManager {
    val pairedDevices: StateFlow<List<BluetoothDeviceUI>>
    val scannedDevices: StateFlow<List<BluetoothDeviceUI>>
    val errors: SharedFlow<String>
    fun startDiscovery()
    fun stopDiscovery()
    fun release()
}
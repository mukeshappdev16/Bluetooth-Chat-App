package com.ms.bluetoothchatapp.domain.discovery

import com.ms.bluetoothchatapp.domain.model.BluetoothDeviceUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface DiscoveryManager {
    val pairedDevices: StateFlow<List<BluetoothDeviceUI>>
    val scannedDevices: StateFlow<List<BluetoothDeviceUI>>
    fun startDiscovery()
    fun stopDiscovery()
}
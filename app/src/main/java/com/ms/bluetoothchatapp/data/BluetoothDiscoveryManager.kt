package com.ms.bluetoothchatapp.data

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import com.ms.bluetoothchatapp.domain.discovery.DiscoveryManager
import com.ms.bluetoothchatapp.domain.model.BluetoothDeviceUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BluetoothDiscoveryManager @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT) constructor(
    val context: Context,
    val bluetoothAdapter: BluetoothAdapter?
) : DiscoveryManager {

    private val _pairedDevices: MutableStateFlow<List<BluetoothDeviceUI>> =
        MutableStateFlow(emptyList())

    override val pairedDevices: StateFlow<List<BluetoothDeviceUI>> = _pairedDevices.asStateFlow()

    private val _scannedDevices: MutableStateFlow<List<BluetoothDeviceUI>> =
        MutableStateFlow(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceUI>> = _scannedDevices.asStateFlow()

    init {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        if (!pairedDevices.isNullOrEmpty()) {
            _pairedDevices.value = pairedDevices.map { it.toBluetoothDeviceUI() }
        }
    }

    val bluetoothReceiver = object : BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.toBluetoothDeviceUI()?.let { uiDevice ->
                    _scannedDevices.update { devices ->
                        if (uiDevice in devices) devices else devices + uiDevice
                    }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun startDiscovery() {
        context.registerReceiver(bluetoothReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        bluetoothAdapter?.startDiscovery()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun stopDiscovery() {
        context.unregisterReceiver(bluetoothReceiver)
        bluetoothAdapter?.cancelDiscovery()
    }
}
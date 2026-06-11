package com.ms.bluetoothchatapp.data

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresPermission
import com.ms.bluetoothchatapp.domain.discovery.DiscoveryManager
import com.ms.bluetoothchatapp.domain.model.BluetoothDeviceUI
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BluetoothDiscoveryManager @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT) constructor(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) : DiscoveryManager {

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceUI>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceUI>> = _pairedDevices.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceUI>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceUI>> = _scannedDevices.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String> = _errors.asSharedFlow()

    private val bluetoothReceiver = object : BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.toBluetoothDeviceUI()?.let { uiDevice ->
                        _scannedDevices.update { devices ->
                            if (devices.any { it.address == uiDevice.address }) devices else devices + uiDevice
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    // Discovery finished
                }
            }
        }
    }

    init {
        updatePairedDevices()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun updatePairedDevices() {
        try {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            _pairedDevices.update {
                pairedDevices?.map { it.toBluetoothDeviceUI() } ?: emptyList()
            }
        } catch (e: SecurityException) {
            _errors.tryEmit("Missing permission for paired devices")
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override fun startDiscovery() {
        if (bluetoothAdapter == null) {
            _errors.tryEmit("Bluetooth not supported")
            return
        }

        updatePairedDevices()
        _scannedDevices.update { emptyList() }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(bluetoothReceiver, filter)
        
        val discoveryStarted = bluetoothAdapter.startDiscovery()
        if (!discoveryStarted) {
            _errors.tryEmit("Failed to start discovery")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun stopDiscovery() {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        try {
            context.unregisterReceiver(bluetoothReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun release() {
        stopDiscovery()
    }
}

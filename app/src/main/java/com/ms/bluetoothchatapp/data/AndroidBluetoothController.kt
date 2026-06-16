package com.ms.bluetoothchatapp.data

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresPermission
import com.ms.bluetoothchatapp.domain.chat.BluetoothController
import com.ms.bluetoothchatapp.domain.chat.BluetoothMessage
import com.ms.bluetoothchatapp.domain.chat.ConnectionResult
import com.ms.bluetoothchatapp.domain.model.BluetoothDeviceUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.util.UUID

class AndroidBluetoothController(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) : BluetoothController {

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    override val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceUI>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceUI>> = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceUI>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceUI>> = _pairedDevices.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String> = _errors.asSharedFlow()

    private val _messages = MutableStateFlow<List<BluetoothMessage>>(emptyList())
    override val messages: StateFlow<List<BluetoothMessage>> = _messages.asStateFlow()

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
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isDiscovering.update { true }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isDiscovering.update { false }
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    _isConnected.update { true }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    _isConnected.update { false }
                    _messages.update { emptyList() }
                }
            }
        }
    }

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null
    private var dataTransferService: BluetoothDataTransferService? = null

    init {
        updatePairedDevices()
        context.registerReceiver(
            bluetoothReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
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
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return createConnectionFlow {
            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(SERVICE_UUID)
            )
            currentServerSocket?.accept()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connectToDevice(device: BluetoothDeviceUI): Flow<ConnectionResult> {
        return createConnectionFlow {
            val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)
            currentClientSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(
                UUID.fromString(SERVICE_UUID)
            )
            stopDiscovery()
            currentClientSocket?.apply { connect() }
        }
    }

    private fun createConnectionFlow(
        connectionBlock: suspend () -> BluetoothSocket?
    ): Flow<ConnectionResult> {
        return flow {
            try {
                val socket = connectionBlock()
                if (socket == null) {
                    emit(ConnectionResult.Error("Connection failed"))
                    return@flow
                }
                
                currentClientSocket = socket
                emit(ConnectionResult.ConnectionEstablished)
                
                val service = BluetoothDataTransferService(socket)
                dataTransferService = service
                
                _isConnected.update { true }
                
                emitAll(
                    service.listenForIncomingMessages()
                        .map { message ->
                            _messages.update { it + message }
                            ConnectionResult.ConnectionEstablished
                        }
                )
            } catch (e: IOException) {
                closeConnection()
                emit(ConnectionResult.Error("Connection was interrupted"))
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun trySendMessage(message: String): BluetoothMessage? {
        if (!isConnected.value || dataTransferService == null) return null
        
        val bluetoothMessage = BluetoothMessage(
            message = message,
            senderName = try { bluetoothAdapter?.name ?: "Unknown" } catch (e: SecurityException) { "Unknown" },
            isFromLocalUser = true
        )

        val success = dataTransferService?.sendMessage(message.toByteArray()) == true
        if (success) {
            _messages.update { it + bluetoothMessage }
            return bluetoothMessage
        }
        return null
    }

    override fun closeConnection() {
        try {
            currentClientSocket?.close()
            currentServerSocket?.close()
        } catch (e: IOException) {}
        currentClientSocket = null
        currentServerSocket = null
        _isConnected.update { false }
        _messages.update { emptyList() }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun release() {
        stopDiscovery()
        closeConnection()
        try {
            context.unregisterReceiver(bluetoothReceiver)
        } catch (e: Exception) {}
    }

    companion object {
        const val SERVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }
}

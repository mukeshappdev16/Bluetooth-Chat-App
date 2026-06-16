package com.ms.bluetoothchatapp.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ms.bluetoothchatapp.domain.chat.BluetoothController
import com.ms.bluetoothchatapp.domain.chat.ConnectionResult
import com.ms.bluetoothchatapp.domain.model.BluetoothDeviceUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    val pairedDevices = bluetoothController.pairedDevices
    val scannedDevices = bluetoothController.scannedDevices
    val isConnected = bluetoothController.isConnected
    val isDiscovering = bluetoothController.isDiscovering

    val state = combine(
        pairedDevices,
        scannedDevices,
        isDiscovering,
        isConnecting,
        isConnected
    ) { paired, scanned, discovering, connecting, connected ->
        SearchUiState(
            pairedDevices = paired,
            scannedDevices = scanned,
            isDiscovering = discovering,
            isConnecting = connecting,
            isConnected = connected
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    private var connectionJob: Job? = null

    fun startDiscovery() {
        bluetoothController.startDiscovery()
    }

    fun stopDiscovery() {
        bluetoothController.stopDiscovery()
    }

    fun startServer() {
        _isConnecting.update { true }
        connectionJob = bluetoothController
            .startBluetoothServer()
            .listen()
    }

    fun connectToDevice(device: BluetoothDeviceUI) {
        _isConnecting.update { true }
        connectionJob = bluetoothController
            .connectToDevice(device)
            .listen()
    }

    fun disconnect() {
        bluetoothController.closeConnection()
        _isConnecting.update { false }
    }

    private fun kotlinx.coroutines.flow.Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when (result) {
                ConnectionResult.ConnectionEstablished -> {
                    _isConnecting.update { false }
                }
                is ConnectionResult.Error -> {
                    _isConnecting.update { false }
                }
            }
        }.catch {
            _isConnecting.update { false }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}

data class SearchUiState(
    val pairedDevices: List<BluetoothDeviceUI> = emptyList(),
    val scannedDevices: List<BluetoothDeviceUI> = emptyList(),
    val isDiscovering: Boolean = false,
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val errorMessage: String? = null
)

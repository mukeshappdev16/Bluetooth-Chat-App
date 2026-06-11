package com.ms.bluetoothchatapp.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ms.bluetoothchatapp.domain.discovery.DiscoveryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val discoveryManager: DiscoveryManager
) : ViewModel() {

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering = _isDiscovering.asStateFlow()

    val pairedDevices = discoveryManager.pairedDevices
    val scannedDevices = discoveryManager.scannedDevices
    val errors = discoveryManager.errors

    val state = combine(
        pairedDevices,
        scannedDevices,
        isDiscovering
    ) { paired, scanned, discovering ->
        SearchUiState(
            pairedDevices = paired,
            scannedDevices = scanned,
            isDiscovering = discovering
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    fun startDiscovery() {
        _isDiscovering.value = true
        discoveryManager.startDiscovery()
    }

    fun stopDiscovery() {
        _isDiscovering.value = false
        discoveryManager.stopDiscovery()
    }

    override fun onCleared() {
        super.onCleared()
        discoveryManager.release()
    }
}

data class SearchUiState(
    val pairedDevices: List<com.ms.bluetoothchatapp.domain.model.BluetoothDeviceUI> = emptyList(),
    val scannedDevices: List<com.ms.bluetoothchatapp.domain.model.BluetoothDeviceUI> = emptyList(),
    val isDiscovering: Boolean = false,
    val errorMessage: String? = null
)

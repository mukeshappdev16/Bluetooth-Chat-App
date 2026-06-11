package com.ms.bluetoothchatapp.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ms.bluetoothchatapp.domain.discovery.DiscoveryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val discoveryManager: DiscoveryManager) : ViewModel() {

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering = _isDiscovering.asStateFlow()

    val pairedDevices = discoveryManager.pairedDevices
    val scannedDevices = discoveryManager.scannedDevices

    fun startDiscovery() {
        _isDiscovering.value = true
        discoveryManager.startDiscovery()
    }

    fun stopDiscovery() {
        _isDiscovering.value = false
        discoveryManager.stopDiscovery()
    }

}
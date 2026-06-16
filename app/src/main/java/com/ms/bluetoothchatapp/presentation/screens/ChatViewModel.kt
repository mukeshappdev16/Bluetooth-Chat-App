package com.ms.bluetoothchatapp.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ms.bluetoothchatapp.domain.chat.BluetoothController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    private val _messageText = MutableStateFlow("")
    val messageText = _messageText.asStateFlow()

    val messages = bluetoothController.messages
    val isConnected = bluetoothController.isConnected

    val state = combine(
        messages,
        isConnected,
        _messageText
    ) { messages, isConnected, messageText ->
        ChatUiState(
            messages = messages,
            isConnected = isConnected,
            messageText = messageText
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatUiState())

    fun onMessageChange(message: String) {
        _messageText.value = message
    }

    fun sendMessage() {
        viewModelScope.launch {
            bluetoothController.trySendMessage(_messageText.value)
            _messageText.value = ""
        }
    }

    fun disconnect() {
        bluetoothController.closeConnection()
    }
}

data class ChatUiState(
    val messages: List<com.ms.bluetoothchatapp.domain.chat.BluetoothMessage> = emptyList(),
    val isConnected: Boolean = false,
    val messageText: String = ""
)

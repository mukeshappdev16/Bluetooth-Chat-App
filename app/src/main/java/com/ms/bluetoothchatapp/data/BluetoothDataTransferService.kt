package com.ms.bluetoothchatapp.data

import android.Manifest
import android.bluetooth.BluetoothSocket
import androidx.annotation.RequiresPermission
import com.ms.bluetoothchatapp.domain.chat.BluetoothMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothDataTransferService(
    private val socket: BluetoothSocket
) {
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun listenForIncomingMessages(): Flow<BluetoothMessage> {
        return flow {
            if (!socket.isConnected) {
                return@flow
            }
            val buffer = ByteArray(1024)
            while (true) {
                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    throw IOException("Reading from input stream failed")
                }

                emit(
                    BluetoothMessage(
                        message = buffer.decodeToString(endIndex = byteCount),
                        senderName = try { socket.remoteDevice.name ?: "Unknown" } catch (e: SecurityException) { "Unknown" },
                        isFromLocalUser = false
                    )
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }
}

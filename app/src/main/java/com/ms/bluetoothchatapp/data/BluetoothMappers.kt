package com.ms.bluetoothchatapp.data

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import com.ms.bluetoothchatapp.domain.model.BluetoothDeviceUI

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun BluetoothDevice.toBluetoothDeviceUI(): BluetoothDeviceUI {
    return BluetoothDeviceUI(
        name = this.name,
        address = this.address
    )
}
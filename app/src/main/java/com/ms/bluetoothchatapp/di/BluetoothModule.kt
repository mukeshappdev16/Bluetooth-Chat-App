package com.ms.bluetoothchatapp.di

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import com.ms.bluetoothchatapp.data.AndroidBluetoothController
import com.ms.bluetoothchatapp.domain.chat.BluetoothController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule {

    @Provides
    @Singleton
    fun provideBluetoothAdapter(@ApplicationContext context: Context): BluetoothAdapter? {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter
    }

    @Provides
    @Singleton
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun provideBluetoothController(
        @ApplicationContext context: Context,
        bluetoothAdapter: BluetoothAdapter?
    ): BluetoothController {
        return AndroidBluetoothController(context, bluetoothAdapter)
    }
}

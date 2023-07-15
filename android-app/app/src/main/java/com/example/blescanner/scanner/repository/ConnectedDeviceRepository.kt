package com.example.blescanner.scanner.repository

import android.util.Log
import com.example.blescanner.model.BluetoothScannedDevice
import com.example.blescanner.scanner.service.BluetoothClientService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConnectedDeviceRepository(
    private val bluetoothClientService: BluetoothClientService,
    coroutineScope: CoroutineScope
) {
    private val connectedDevices: MutableSet<BluetoothScannedDevice> = mutableSetOf()

    private val connectedDevicesStream: MutableStateFlow<List<BluetoothScannedDevice>> =
        MutableStateFlow(emptyList())

    init {
        coroutineScope.launch {
            bluetoothClientService.deviceConnectionEvent.collect {
                Log.d("repository", "connected $it")
                connectedDevices.add(it)
                connectedDevicesStream.update { connectedDevices.toList() }
            }
        }

        coroutineScope.launch {
            bluetoothClientService.deviceDisconnectionEvent.collect {
                Log.d("repository", "disconnected $it")
                connectedDevices.remove(it)
                connectedDevicesStream.update { connectedDevices.toList() }
            }
        }
    }

    fun streamAll(): StateFlow<List<BluetoothScannedDevice>> {
        return connectedDevicesStream.asStateFlow()
    }
}
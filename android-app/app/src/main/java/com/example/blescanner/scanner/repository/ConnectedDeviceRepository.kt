package com.example.blescanner.scanner.repository

import android.util.Log
import com.example.blescanner.model.BluetoothSession
import com.example.blescanner.scanner.service.BluetoothClientService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConnectedDeviceRepository(
    private val bluetoothClientService: BluetoothClientService,
    coroutineScope: CoroutineScope
) {

    companion object {
        private val TAG = ConnectedDeviceRepository::class.simpleName
    }
    private val connectedDevices: MutableSet<BluetoothSession> = mutableSetOf()

    private val connectedDevicesStream: MutableStateFlow<List<BluetoothSession>> =
        MutableStateFlow(emptyList())

    private val _deviceRemovedEvent: MutableSharedFlow<BluetoothSession> =
        MutableSharedFlow()
    val deviceRemovedEvent: SharedFlow<BluetoothSession> =
        _deviceRemovedEvent.asSharedFlow()

    init {
        coroutineScope.launch {
            bluetoothClientService.deviceConnectionEvent.collect {
                Log.d(TAG, "connected $it")
                connectedDevices.add(it)
                connectedDevicesStream.update { connectedDevices.toList() }
            }
        }

        coroutineScope.launch {
            bluetoothClientService.deviceDisconnectionEvent.collect {
                Log.d(TAG, "disconnected $it")
                connectedDevices.remove(it)
                connectedDevicesStream.update { connectedDevices.toList() }
                _deviceRemovedEvent.emit(it)
            }
        }
    }

    fun streamAll(): StateFlow<List<BluetoothSession>> {
        return connectedDevicesStream.asStateFlow()
    }
}
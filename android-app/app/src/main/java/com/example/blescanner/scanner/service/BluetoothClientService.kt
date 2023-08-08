package com.example.blescanner.scanner.service

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import com.example.blescanner.model.BluetoothSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class BluetoothClientService(
    bluetoothManager: BluetoothManager,
    private val coroutineScope: CoroutineScope,
    private val context: Context
) {
    companion object {
        private val TAG = BluetoothClientService::class.simpleName
    }

    private val bluetoothAdapter = bluetoothManager.adapter

    private val connectingSessions: MutableMap<String, BluetoothSession> = mutableMapOf()

    private val connectedSessions: MutableMap<String, BluetoothSession> =
        mutableMapOf()

    private val _deviceConnectionEvent: MutableSharedFlow<BluetoothSession> =
        MutableSharedFlow()
    val deviceConnectionEvent: SharedFlow<BluetoothSession> =
        _deviceConnectionEvent.asSharedFlow()

    private val _deviceDisconnectionEvent: MutableSharedFlow<BluetoothSession> =
        MutableSharedFlow()
    val deviceDisconnectionEvent: SharedFlow<BluetoothSession> =
        _deviceDisconnectionEvent.asSharedFlow()

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    fun connect(address: String) {
        coroutineScope.launch {
            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
            val newBluetoothSession = BluetoothSession(bluetoothDevice, coroutineScope, context)
            launch {
                newBluetoothSession.connectionEvent.collect { address ->
                    val connectedSession = connectingSessions.get(address)
                    connectedSessions.remove(address)
                    connectedSession?.let {
                        connectedSessions[address] = it
                        _deviceConnectionEvent.emit(it)
                    }
                }
            }
            launch {
                newBluetoothSession.disconnectionEvent.collect { address ->
                    val disconnectedSession = connectedSessions.get(address)
                    connectedSessions.remove(address)
                    disconnectedSession?.let {
                        _deviceDisconnectionEvent.emit(it)
                    }
                }
            }
            connectingSessions[address] = newBluetoothSession
            newBluetoothSession.connect()
        }
    }
}
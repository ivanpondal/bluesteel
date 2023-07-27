package com.example.blescanner.scanner.service

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.blescanner.model.BluetoothScannedDevice
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

    private val connectedGatts: MutableMap<String, BluetoothGatt> =
        mutableMapOf()

    private val _deviceConnectionEvent: MutableSharedFlow<BluetoothScannedDevice> =
        MutableSharedFlow()
    val deviceConnectionEvent: SharedFlow<BluetoothScannedDevice> =
        _deviceConnectionEvent.asSharedFlow()

    private val _deviceDisconnectionEvent: MutableSharedFlow<BluetoothScannedDevice> =
        MutableSharedFlow()
    val deviceDisconnectionEvent: SharedFlow<BluetoothScannedDevice> =
        _deviceDisconnectionEvent.asSharedFlow()

    private val gattClientCallback = object : BluetoothGattCallback() {
        @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            val gattStatus = if (BluetoothGatt.GATT_SUCCESS == status) {
                "success"
            } else if (BluetoothGatt.GATT_FAILURE == status) {
                "failure"
            } else {
                "unknown $status"
            }

            val state = if (BluetoothGatt.STATE_CONNECTED == newState) {
                "connected"
            } else if (BluetoothGatt.STATE_CONNECTING == newState) {
                "connecting"
            } else if (BluetoothGatt.STATE_DISCONNECTED == newState) {
                "disconnected"
            } else {
                "unknown $newState"
            }

            Log.d(TAG, "connection state change, status: $gattStatus $state, state: $state $newState")

            if (gattStatus == "success" && state == "connected" && gatt !== null) {
                connectedGatts[gatt.device.address] = gatt
                coroutineScope.launch {
                    gatt.device.let {
                        _deviceConnectionEvent.emit(
                            BluetoothScannedDevice(
                                it.address, 0, it.name,
                                emptyList()
                            )
                        )
                    }
                }
            } else {
                Log.d(TAG, "something failed $gattStatus $state $status $newState")
                gatt?.close()
                coroutineScope.launch {
                    gatt?.device?.let {
                        _deviceDisconnectionEvent.emit(
                            BluetoothScannedDevice(
                                it.address, 0, it.name,
                                emptyList()
                            )
                        )
                    }
                }
            }
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    fun connect(address: String) {
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothDevice.connectGatt(
                context,
                false,
                gattClientCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        } else {
            bluetoothDevice.connectGatt(context, false, gattClientCallback)
        }
    }
}
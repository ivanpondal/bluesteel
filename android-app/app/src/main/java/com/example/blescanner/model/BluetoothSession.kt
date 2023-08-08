package com.example.blescanner.model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class BluetoothSession(
    private val bluetoothDevice: BluetoothDevice,
    private val coroutineScope: CoroutineScope,
    private val context: Context
) : Identifiable<String> {

    companion object {
        private val TAG = BluetoothSession::class.simpleName
    }

    override val id: String
        get() = bluetoothDevice.address

    private var bluetoothGatt: BluetoothGatt? = null

    private val _connectionEvent: MutableSharedFlow<String> = MutableSharedFlow()
    val connectionEvent: SharedFlow<String> = _connectionEvent.asSharedFlow()

    private val _disconnectionEvent: MutableSharedFlow<String> = MutableSharedFlow()
    val disconnectionEvent: SharedFlow<String> = _disconnectionEvent.asSharedFlow()

    private val discoveryChannel = Channel<Boolean>()

    @SuppressLint("MissingPermission")
    suspend fun discoverServices() {
        coroutineScope.launch {
            bluetoothGatt?.let {
                it.discoverServices()
                discoveryChannel.receive()
            }
        }
    }

    private val gattClientCallback = object : BluetoothGattCallback() {
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            coroutineScope.launch {
                Log.d(TAG, "Found services for ${this@BluetoothSession.id}: ${gatt?.services}")
                discoveryChannel.send(true)
            }
        }

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

            Log.d(
                TAG,
                "connection state change, status: $gattStatus $status, state: $state $newState"
            )

            if (gattStatus == "success" && state == "connected" && gatt !== null) {
                coroutineScope.launch {
                    gatt.device.let { _connectionEvent.emit(this@BluetoothSession.id) }
                }
                bluetoothGatt = gatt
            } else {
                Log.d(
                    TAG,
                    "something failed, status: $gattStatus $status, state: $state $newState"
                )
                gatt?.let {
                    coroutineScope.launch {
                        _disconnectionEvent.emit(this@BluetoothSession.id)
                    }
                    it.close()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connect() {
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
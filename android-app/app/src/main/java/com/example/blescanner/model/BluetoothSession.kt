package com.example.blescanner.model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
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
import java.util.UUID

class BluetoothSession(
    private val bluetoothDevice: BluetoothDevice,
    private val coroutineScope: CoroutineScope,
    private val context: Context
) : Identifiable<String> {

    companion object {
        private val TAG = BluetoothSession::class.simpleName
        private const val DEFAULT_ATT_MTU = 23
        const val MAX_ATT_MTU = 517
    }

    override val id: String
        get() = bluetoothDevice.address

    private var bluetoothGatt: BluetoothGatt? = null

    private val _connectionEvent: MutableSharedFlow<String> = MutableSharedFlow()
    val connectionEvent: SharedFlow<String> = _connectionEvent.asSharedFlow()

    private val _disconnectionEvent: MutableSharedFlow<String> = MutableSharedFlow()
    val disconnectionEvent: SharedFlow<String> = _disconnectionEvent.asSharedFlow()

    private val discoveryChannel = Channel<Boolean>()
    private val mtuRequestChannel = Channel<Int>()
    private val writeWithResponseChannel = Channel<Boolean>()

    @SuppressLint("MissingPermission")
    suspend fun requestMtu(value: Int): Int {
        return bluetoothGatt?.let {
            it.requestMtu(value)
            mtuRequestChannel.receive()
        } ?: DEFAULT_ATT_MTU
    }

    @SuppressLint("MissingPermission")
    suspend fun discoverServices() {
        bluetoothGatt?.let {
            it.discoverServices()
            discoveryChannel.receive()
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun writeWithResponse(serviceUUID: UUID, characteristicUUID: UUID, message: ByteArray) {
        bluetoothGatt?.let { gatt ->
            gatt.getService(serviceUUID).let { service ->
                service.getCharacteristic(characteristicUUID).let { characteristic ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeCharacteristic(
                            characteristic,
                            message,
                            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        )
                    } else {
                        characteristic.writeType =
                            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        characteristic.value = message
                        gatt.writeCharacteristic(characteristic)
                    }
                    writeWithResponseChannel.receive()
                }
            }
        }
    }

    private val gattClientCallback = object : BluetoothGattCallback() {
        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    coroutineScope.launch {
                        mtuRequestChannel.send(mtu)
                    }
                }

                else -> {
                    coroutineScope.launch {
                        mtuRequestChannel.send(DEFAULT_ATT_MTU)
                    }
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

            with(characteristic) {
                var success = false
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        success = true
                        coroutineScope.launch { writeWithResponseChannel.send(true) }
                    }

                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e(TAG, "Write exceeded connection ATT MTU!")
                    }

                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e(TAG, "Write not permitted for ${this?.uuid}!")
                    }

                    else -> {
                        Log.e(
                            TAG,
                            "Characteristic write failed for ${this?.uuid}, error: $status"
                        )
                    }
                }
                if (!success) {
                    coroutineScope.launch { writeWithResponseChannel.send(false) }
                }
            }
        }

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
                    bluetoothGatt = null
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

    @SuppressLint("MissingPermission")
    fun close() {
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }
}
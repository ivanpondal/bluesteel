package com.example.blescanner.advertiser

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

@SuppressLint("MissingPermission")
class BluetoothServer(
    bluetoothManager: BluetoothManager, context: Context,
    private val coroutineScope: CoroutineScope,
) {

    companion object {
        private val TAG = BluetoothServer::class.simpleName
    }

    private lateinit var gattServer: BluetoothGattServer

    private val servicePublishingChannel = Channel<Boolean>()
    private val advertisingChannel = Channel<Boolean>()

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(
            device: android.bluetooth.BluetoothDevice?,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(device, status, newState)

            Log.d(
                TAG,
                "onConnectionStateChange: Server $device status: $status state: $newState"
            )
        }

        @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
        override fun onCharacteristicWriteRequest(
            device: android.bluetooth.BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )

            value?.let {
                val message = String(value, StandardCharsets.UTF_8);
                Log.d(
                    TAG,
                    "Received value with offset $offset $responseNeeded from $device: $message"
                )
            }

            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)

            coroutineScope.launch {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> servicePublishingChannel.send(true)
                    else -> servicePublishingChannel.send(false)
                }
            }
        }
    }

    private var advertiser: BluetoothLeAdvertiser

    private val advertisementCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertising successfully started")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.d(TAG, "Advertising failed with error:  $errorCode")
        }
    }

    init {
        gattServer = bluetoothManager.openGattServer(context, this.gattServerCallback)
        advertiser = bluetoothManager.adapter.bluetoothLeAdvertiser
    }

    suspend fun publishService(gattService: GattService): Boolean {
        val service =
            BluetoothGattService(gattService.serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        // need to ensure that the property is writable and has the write permission
        val messageCharacteristic = BluetoothGattCharacteristic(
            gattService.characteristicUUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(messageCharacteristic)

        gattServer.addService(service)

        return servicePublishingChannel.receive()
    }

    @SuppressLint("MissingPermission")
    suspend fun startAdvertising(gattService: GattService):Boolean {
        advertiser.startAdvertising(
            AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                .build(),
            AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(gattService.serviceUUID))
                .build(),
            advertisementCallback
        )

        return advertisingChannel.receive()
    }

    @SuppressLint("MissingPermission")
    suspend fun stopAdvertising() {
        advertiser.stopAdvertising(advertisementCallback)
        advertisingChannel.send(true)
    }
}
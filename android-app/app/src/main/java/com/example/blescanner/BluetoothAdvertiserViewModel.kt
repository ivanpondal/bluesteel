package com.example.blescanner

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import com.example.blescanner.scanner.service.BluetoothConstants.CHARACTERISTIC_UUID
import com.example.blescanner.scanner.service.BluetoothConstants.SERVICE_UUID
import kotlinx.coroutines.flow.*
import java.nio.charset.StandardCharsets
import java.util.*


class BluetoothAdvertiserViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private val TAG = BluetoothAdvertiserViewModel::class.simpleName
    }

    private val bluetoothManager: BluetoothManager by lazy {
        application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private var gattServer: BluetoothGattServer? = null
    val bluetoothEnabled = bluetoothAdapter.isEnabled

    private val advertisementCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertising successfully started")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            // Send error state to display
            val errorMessage = "Advertise failed with error: $errorCode"
            Log.d(TAG, "Advertising failed:  $errorMessage")
        }
    }

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

            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
        }
    }

    private fun setupGattService(): BluetoothGattService {
        // Setup gatt service
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        // need to ensure that the property is writable and has the write permission
        val messageCharacteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(messageCharacteristic)

        return service
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_ADVERTISE", "android.permission.BLUETOOTH_CONNECT"])
    fun startAdvertisement() {
        gattServer = bluetoothManager.openGattServer(getApplication(), gattServerCallback)
        gattServer?.addService(setupGattService())

        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser

        advertiser.startAdvertising(
            AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                .build(),
            AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(SERVICE_UUID))
                .build(),
            advertisementCallback
        )
    }
}
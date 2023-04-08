package com.example.blescanner

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import com.example.blescanner.model.BluetoothDevice
import kotlinx.coroutines.flow.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*


private val SERVICE_UUID = UUID.fromString("FE4B1073-17BB-4982-955F-28702F277F19")
private val CHARACTERISTIC_UUID = UUID.fromString("A5C46D55-280D-4B9E-8335-BCA4C0977BDB")

class BluetoothDevicesViewModel(application: Application) : AndroidViewModel(application) {


    private val scannedDevices: MutableSet<BluetoothDevice> = mutableSetOf()

    private val _bluetoothDevices: MutableStateFlow<List<BluetoothDevice>> =
        MutableStateFlow(emptyList())
    val bluetoothDevices: Flow<List<BluetoothDevice>> = _bluetoothDevices.debounce(1000)

    private val bluetoothManager: BluetoothManager by lazy {
        application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private val connectedGatts: MutableMap<String, BluetoothGatt> =
        mutableMapOf()


    val bluetoothEnabled = bluetoothAdapter.isEnabled

    private val scanCallback = object : ScanCallback() {
        // Permission should have been asked in main activity
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val scannedDevice = BluetoothDevice(
                id = result.device.address,
                rssi = result.rssi,
                name = result.device.name,
                advertisements = result.scanRecord?.serviceUuids ?: emptyList()
            )
            scannedDevices.remove(scannedDevice)
            scannedDevices.add(scannedDevice)

            _bluetoothDevices.update { scannedDevices.toList().sortedByDescending { it.rssi } }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "onScanFailed: code $errorCode")
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    fun startScan() {
        bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    fun stopScan() {
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

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
                Log.d(TAG, "Received value from $device: $message")
            }
        }
    }

    private fun setupGattService(): BluetoothGattService {
        // Setup gatt service
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        // need to ensure that the property is writable and has the write permission
        val messageCharacteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(messageCharacteristic)

        return service
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_ADVERTISE", "android.permission.BLUETOOTH_CONNECT"])
    fun startAdvertisement() {
        val gattServer = bluetoothManager.openGattServer(getApplication(), gattServerCallback)
        gattServer.addService(setupGattService())

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

            Log.d(TAG, "connection state change, status: $gattStatus, state: $state")

            if (gattStatus == "success" && state == "connected" && gatt !== null) {
                connectedGatts[gatt.device.address] = gatt
                gatt.discoverServices()
            } else {
                gatt?.close()
            }
        }

        @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status == BluetoothGatt.GATT_SUCCESS && gatt !== null) {
                val gattService = gatt.getService(SERVICE_UUID)

                gattService?.let { service ->
                    val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
                    val message = "mE pERdonAs?".toByteArray(charset = Charsets.UTF_8)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeCharacteristic(
                            characteristic,
                            message,
                            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                        )
                    } else {
                        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                        characteristic.value= message
                        gatt.writeCharacteristic(characteristic)
                    }
                }
            } else {
                Log.d(TAG, "discovery failed with status $status")
            }
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    fun connectGatt(id: String) {
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothDevice.connectGatt(
                getApplication(),
                false,
                gattClientCallback,
                android.bluetooth.BluetoothDevice.TRANSPORT_LE
            )
        } else {
            bluetoothDevice.connectGatt(getApplication(), false, gattClientCallback)
        }
    }
}
package com.example.blescanner

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import com.example.blescanner.model.BluetoothDevice
import kotlinx.coroutines.flow.*
import java.util.*

class BluetoothDevicesViewModel(application: Application) : AndroidViewModel(application) {

    private val scannedDevices: MutableSet<BluetoothDevice> = mutableSetOf()

    private val _bluetoothDevices: MutableStateFlow<List<BluetoothDevice>> =
        MutableStateFlow(emptyList())
    val bluetoothDevices: Flow<List<BluetoothDevice>> = _bluetoothDevices.debounce(1000)

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

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

    @RequiresPermission(value = "android.permission.BLUETOOTH_ADVERTISE")
    fun startAdvertisement() {
        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser

        advertiser.startAdvertising(
            AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                .build(),
            AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(UUID.fromString("FE4B1073-17BB-4982-955F-28702F277F19")))
                .build(),
            advertisementCallback
        )
    }
}
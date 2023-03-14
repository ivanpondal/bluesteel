package com.example.blescanner

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import com.example.blescanner.model.BluetoothDevice
import kotlinx.coroutines.flow.*

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
}
package com.example.blescanner.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.blescanner.model.BluetoothDevice
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

private val SERVICE_UUID = UUID.fromString("FE4B1073-17BB-4982-955F-28702F277F19")

class BluetoothScanner(private val bluetoothManager: BluetoothManager){
    // Add extra buffer capacity because default is 0 and tryEmit fails to emit
    private val _scannedDeviceEvent: MutableSharedFlow<BluetoothDevice> = MutableSharedFlow(extraBufferCapacity = 10)
    val scannedDeviceEvent: SharedFlow<BluetoothDevice> = _scannedDeviceEvent

    private val bluetoothLeScanner: BluetoothLeScanner by lazy {
        bluetoothManager.adapter.bluetoothLeScanner
    }

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

            _scannedDeviceEvent.tryEmit(scannedDevice)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(BluetoothScanner::class.simpleName, "onScanFailed: code $errorCode")
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    fun startScan() {
        bluetoothLeScanner.startScan(
            listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()),
            ScanSettings.Builder().build(),
            scanCallback
        )
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    fun stopScan() {
        bluetoothLeScanner.stopScan(scanCallback)
    }
}
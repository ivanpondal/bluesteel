package com.example.blescanner.scanner.service

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
import com.example.blescanner.model.BluetoothDeviceAdvertisement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

class BluetoothScanner(
    private val bluetoothManager: BluetoothManager,
    private val coroutineScope: CoroutineScope
) {
    // Add extra buffer capacity because default is 0 and tryEmit fails to emit
    private val _scannedDeviceEvent: MutableSharedFlow<BluetoothDeviceAdvertisement> =
        MutableSharedFlow(extraBufferCapacity = 10)
    val scannedDeviceEvent: SharedFlow<BluetoothDeviceAdvertisement> = _scannedDeviceEvent

    private val bluetoothLeScanner: BluetoothLeScanner by lazy {
        bluetoothManager.adapter.bluetoothLeScanner
    }

    private val scanCallback = object : ScanCallback() {
        // Permission should have been asked in main activity
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val scannedDevice = BluetoothDeviceAdvertisement(
                id = result.device.address,
                rssi = result.rssi,
                name = result.device.name,
                services = result.scanRecord?.serviceUuids ?: emptyList()
            )

            coroutineScope.launch {
                Log.d(
                    BluetoothScanner::class.simpleName,
                    "Found device matching scan filter: $scannedDevice"
                )
                _scannedDeviceEvent.emit(scannedDevice)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d(
                BluetoothScanner::class.simpleName,
                "Batch scan?"
            )
        }

        override fun onScanFailed(errorCode: Int) {
            when (errorCode) {
                SCAN_FAILED_INTERNAL_ERROR ->
                    Log.e(
                        BluetoothScanner::class.simpleName,
                        "onScanFailed: SCAN_FAILED_INTERNAL_ERROR"
                    )

                else ->
                    Log.e(BluetoothScanner::class.simpleName, "onScanFailed: code $errorCode")
            }
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    fun startScan(serviceUUID: UUID) {
        bluetoothLeScanner.stopScan(scanCallback)

        bluetoothLeScanner.startScan(
            listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(serviceUUID)).build()),
            ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build(),
            scanCallback
        )
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    fun stopScan() {
        bluetoothLeScanner.stopScan(scanCallback)
    }
}
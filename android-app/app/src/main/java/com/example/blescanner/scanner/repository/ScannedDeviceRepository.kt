package com.example.blescanner.scanner.repository

import com.example.blescanner.model.BluetoothScannedDevice
import com.example.blescanner.scanner.service.BluetoothScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ScannedDeviceRepository(
    private val bluetoothScanner: BluetoothScanner,
    private val coroutineScope: CoroutineScope
) {
    private val scannedDevices: MutableSet<BluetoothScannedDevice> = mutableSetOf()

    private val scannedDevicesStream: MutableStateFlow<List<BluetoothScannedDevice>> =
        MutableStateFlow(emptyList())

    init {
        coroutineScope.launch {
            bluetoothScanner.scannedDeviceEvent.debounce(1000).collect { bluetoothDevice ->
                scannedDevices.remove(bluetoothDevice)
                scannedDevices.add(bluetoothDevice)

                scannedDevicesStream.update {
                    scannedDevices.toList().sortedByDescending { it.rssi }
                }
            }
        }
    }

    fun scannedDevicesStream(): StateFlow<List<BluetoothScannedDevice>> {
        return scannedDevicesStream.asStateFlow()
    }
}
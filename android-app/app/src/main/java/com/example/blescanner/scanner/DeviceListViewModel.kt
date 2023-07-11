package com.example.blescanner.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blescanner.model.BluetoothDevice
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class DeviceListViewModel(private val bluetoothScanner: BluetoothScanner) : ViewModel() {

    private val scannedDevices: MutableSet<BluetoothDevice> = mutableSetOf()

    private val _scannedDevicesFlow: MutableStateFlow<List<BluetoothDevice>> =
        MutableStateFlow(emptyList())
    val scannedDevicesFlow: StateFlow<List<BluetoothDevice>> = _scannedDevicesFlow

    init {
        viewModelScope.launch {
            bluetoothScanner.scannedDeviceEvent.debounce(1000).collect { bluetoothDevice ->
                scannedDevices.remove(bluetoothDevice)
                scannedDevices.add(bluetoothDevice)

                _scannedDevicesFlow.update {
                    scannedDevices.toList().sortedByDescending { it.rssi }
                }
            }
        }
    }


    companion object {
        fun provideFactory(
            bluetoothScanner: BluetoothScanner,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DeviceListViewModel(bluetoothScanner = bluetoothScanner) as T
                }
            }
    }
}

package com.example.blescanner.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.blescanner.scanner.repository.ScannedDeviceRepository
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class DeviceListViewModel(private val scannedDeviceRepository: ScannedDeviceRepository) : ViewModel() {
    val scannedDevices = scannedDeviceRepository.streamAll()

    companion object {
        fun provideFactory(
            scannedDeviceRepository: ScannedDeviceRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DeviceListViewModel(scannedDeviceRepository) as T
                }
            }
    }
}

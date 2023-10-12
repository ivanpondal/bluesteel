package com.example.blescanner.devicedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blescanner.model.BluetoothDeviceAdvertisement
import com.example.blescanner.scanner.repository.ScannedDeviceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DeviceDetailViewModel(private val scannedDeviceRepository: ScannedDeviceRepository) :
    ViewModel() {
    fun getScannedDeviceById(id: String): StateFlow<BluetoothDeviceAdvertisement> {
        return scannedDeviceRepository.streamById(id).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            scannedDeviceRepository.findById(id)
        )
    }

    companion object {
        fun provideFactory(
            scannedDeviceRepository: ScannedDeviceRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DeviceDetailViewModel(scannedDeviceRepository) as T
                }
            }
    }
}
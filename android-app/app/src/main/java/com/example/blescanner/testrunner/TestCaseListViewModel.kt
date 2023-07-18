package com.example.blescanner.testrunner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.blescanner.scanner.repository.ConnectedDeviceRepository

class TestCaseListViewModel(private val connectedDeviceRepository: ConnectedDeviceRepository) :
    ViewModel() {

    val connectedDevices = connectedDeviceRepository.streamAll()

    companion object {
        fun provideFactory(
            connectedDeviceRepository: ConnectedDeviceRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TestCaseListViewModel(connectedDeviceRepository) as T
                }
            }
    }
}
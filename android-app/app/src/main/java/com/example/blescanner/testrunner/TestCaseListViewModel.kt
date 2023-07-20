package com.example.blescanner.testrunner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blescanner.scanner.repository.ConnectedDeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TestCaseListViewModel(connectedDeviceRepository: ConnectedDeviceRepository) :
    ViewModel() {
    val connectedDevices = connectedDeviceRepository.streamAll()

    private val selectedDevicesMap: MutableMap<String, Boolean> = mutableMapOf()

    private val _selectedDevices: MutableStateFlow<Map<String, Boolean>> =
        MutableStateFlow(emptyMap())
    val selectedDevices: StateFlow<Map<String, Boolean>> = _selectedDevices

    init {
        viewModelScope.launch {
            connectedDeviceRepository.deviceRemovedEvent.collect {
                selectedDevicesMap.remove(it.id)
                _selectedDevices.update { selectedDevicesMap.toMap() }
            }
        }
    }

    fun toggle(deviceId: String) {
        val oldValue = selectedDevicesMap[deviceId] ?: false
        selectedDevicesMap[deviceId] = !oldValue
        _selectedDevices.update { selectedDevicesMap.toMap() }
    }

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
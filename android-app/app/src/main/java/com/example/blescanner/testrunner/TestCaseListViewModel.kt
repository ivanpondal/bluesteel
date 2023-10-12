package com.example.blescanner.testrunner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blescanner.scanner.repository.ConnectedDeviceRepository
import com.example.blescanner.testrunner.model.TestCaseId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TestCaseListViewModel(connectedDeviceRepository: ConnectedDeviceRepository) :
    ViewModel() {

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

        private val TAG = TestCaseListViewModel::class.simpleName
    }

    val connectedDevices = connectedDeviceRepository.streamAll()

    private val selectedDevicesSet: MutableSet<String> = mutableSetOf()

    private val _selectedDevices: MutableStateFlow<Set<String>> =
        MutableStateFlow(emptySet())
    val selectedDevices: StateFlow<Set<String>> = _selectedDevices

    private val _selectedTestCase: MutableStateFlow<TestCaseId> =
        MutableStateFlow(TestCaseId.SR_OW_1)
    val selectedTestCase: StateFlow<TestCaseId> = _selectedTestCase

    init {
        viewModelScope.launch {
            connectedDeviceRepository.deviceRemovedEvent.collect {
                selectedDevicesSet.remove(it.id)
                _selectedDevices.update { selectedDevicesSet.toSet() }
            }
        }
    }

    fun toggle(deviceId: String) {
        if (selectedDevicesSet.contains(deviceId)) {
            selectedDevicesSet.remove(deviceId)
        } else {
            selectedDevicesSet.add(deviceId)
        }
        _selectedDevices.update { selectedDevicesSet.toSet() }
    }

    fun setTestCase(testCaseId: TestCaseId) {
        _selectedTestCase.update { testCaseId }
    }

}
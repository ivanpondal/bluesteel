package com.example.blescanner.testrunner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blescanner.BluetoothDevicesViewModel.Companion.CHARACTERISTIC_UUID
import com.example.blescanner.BluetoothDevicesViewModel.Companion.SERVICE_UUID
import com.example.blescanner.model.BluetoothSession.Companion.MAX_ATT_MTU
import com.example.blescanner.scanner.repository.ConnectedDeviceRepository
import com.example.blescanner.testrunner.model.TestCaseId
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets.UTF_8

class TestCaseRunViewModel(
    private val connectedDeviceRepository: ConnectedDeviceRepository,
    val testCase: TestCaseId,
    val devices: Set<String>
) :
    ViewModel() {

    companion object {
        fun provideFactory(
            connectedDeviceRepository: ConnectedDeviceRepository,
            testCase: TestCaseId,
            devices: Set<String>
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TestCaseRunViewModel(connectedDeviceRepository, testCase, devices) as T
                }
            }
    }

    fun runTest() {
        val firstDevice = devices.first()
        val session = connectedDeviceRepository.getById(firstDevice)
        viewModelScope.launch {
            session.discoverServices()
            val mtu = session.requestMtu(MAX_ATT_MTU)
            Log.d("TestRunner", "$mtu")
            session.writeWithResponse(
                SERVICE_UUID, CHARACTERISTIC_UUID, "me perdonass??".toByteArray(UTF_8)
            )
        }
    }
}
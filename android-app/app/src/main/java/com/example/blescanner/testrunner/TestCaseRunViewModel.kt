package com.example.blescanner.testrunner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.blescanner.measurements.SystemStopwatch
import com.example.blescanner.scanner.repository.ConnectedDeviceRepository
import com.example.blescanner.testrunner.model.TestCaseId
import com.example.blescanner.testrunner.services.TestRunner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TestCaseRunViewModel(
    private val connectedDeviceRepository: ConnectedDeviceRepository,
    private val workManager: WorkManager,
    val testCase: TestCaseId,
    val devices: Set<String>
) :
    ViewModel() {

    var testResult: String = ""
        private set

    private val _testRunnerState = MutableStateFlow("")
    val testRunnerState = _testRunnerState.asStateFlow()

    private val _testRunnerPacketsSent = MutableStateFlow(0)
    val testRunnerPacketsSent = _testRunnerPacketsSent.asStateFlow()

    private val _testRunnerBytesPerSecond = MutableStateFlow(0f)
    val testRunnerBytesPerSecond = _testRunnerBytesPerSecond.asStateFlow()

    private val _testRunnerMtu = MutableStateFlow(0)
    val testRunnerMtu = _testRunnerMtu.asStateFlow()

    companion object {
        fun provideFactory(
            connectedDeviceRepository: ConnectedDeviceRepository,
            workManager: WorkManager,
            testCase: TestCaseId,
            devices: Set<String>
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TestCaseRunViewModel(
                        connectedDeviceRepository,
                        workManager,
                        testCase,
                        devices
                    ) as T
                }
            }
    }

    fun runTest() {
        val session = if (devices.isNotEmpty()) connectedDeviceRepository.getById(devices.first()) else null
        val testRunner =
            TestRunner(session, SystemStopwatch(), testCase, workManager)


        viewModelScope.launch {
            testRunner.state.collect { _testRunnerState.emit(it) }
        }
        viewModelScope.launch {
            testRunner.packetsSent.collect { _testRunnerPacketsSent.emit(it) }
        }
        viewModelScope.launch {
            testRunner.bytesPerSecond.collect { _testRunnerBytesPerSecond.emit(it) }
        }

        viewModelScope.launch {
            testRunner.mtu.collect { _testRunnerMtu.emit(it) }
        }

        viewModelScope.launch {
            testRunner.run()
            testResult = testRunner.consoleOutput
        }
    }
}
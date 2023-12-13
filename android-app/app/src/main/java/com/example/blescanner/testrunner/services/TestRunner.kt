package com.example.blescanner.testrunner.services

import android.annotation.SuppressLint
import android.util.Log
import com.example.blescanner.advertiser.BluetoothGattService
import com.example.blescanner.measurements.Stopwatch
import com.example.blescanner.model.BluetoothSession
import com.example.blescanner.scanner.service.BluetoothClientService
import com.example.blescanner.scanner.service.BluetoothConstants
import com.example.blescanner.scanner.service.BluetoothConstants.WAKE_CHARACTERISTIC_UUID
import com.example.blescanner.scanner.service.BluetoothConstants.WAKE_SERVICE_UUID
import com.example.blescanner.scanner.service.BluetoothConstants.WRITE_CHARACTERISTIC_UUID
import com.example.blescanner.scanner.service.BluetoothConstants.WRITE_SERVICE_UUID
import com.example.blescanner.scanner.service.BluetoothScanner
import com.example.blescanner.testrunner.model.TestCaseId
import com.example.blescanner.testrunner.model.TestRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class TestRunner(
    private val session: BluetoothSession?,
    private val stopwatch: Stopwatch,
    private val testCase: TestCaseId,
    private val testRole: TestRole,
    private val gattService: BluetoothGattService,
    private val bluetoothScanner: BluetoothScanner,
    private val bluetoothClientService: BluetoothClientService
) {

    companion object {
        private val TAG = TestRunner::class.simpleName

        val RUNNING_EMOJI = "\uD83C\uDFC3\u200D♂️"
        val CHECK_EMOJI = "☑️"
    }

    private val _state = MutableStateFlow("RUNNING $RUNNING_EMOJI")
    val state = _state.asStateFlow()

    private val _packetsSent = MutableStateFlow(0)
    val packetsSent = _packetsSent.asStateFlow()

    private val _bytesPerSecond = MutableStateFlow(0f)
    val bytesPerSecond = _bytesPerSecond.asStateFlow()

    private val _mtu = MutableStateFlow(0)
    val mtu = _mtu.asStateFlow()

    var consoleOutput: String = ""
        private set

    private fun randomArray(size: Int): ByteArray {
        val randomMessage = ByteArray(size)
        Random.Default.nextBytes(randomMessage)
        return randomMessage
    }

    private fun consoleOutput(message: String, stringBuilder: StringBuilder) {
        Log.i(TAG, message)
        stringBuilder.appendLine(message)
    }

    @SuppressLint("MissingPermission")
    suspend fun run() {
        val outputBuilder = StringBuilder()
        when (testCase) {
            TestCaseId.SR_OW_1 -> {
                session?.let {
                    stopwatch.start()
                    it.discoverServices()
                    consoleOutput("service discovery time ${stopwatch.stop()} ms", outputBuilder)

                    stopwatch.start()
                    val mtu = session.requestMtu(BluetoothSession.MAX_ATT_MTU) - 3
                    _mtu.emit(mtu)
                    consoleOutput(
                        "mtu $mtu bytes, request time ${stopwatch.stop()} ms",
                        outputBuilder
                    )
                    var totalTimeSendingInMs = 0L
                    var totalBytesSent = 0
                    repeat(100) {
                        val randomMessage = randomArray(mtu)
                        stopwatch.start()
                        session.writeWithResponse(
                            WRITE_SERVICE_UUID,
                            WRITE_CHARACTERISTIC_UUID,
                            randomMessage
                        )
                        val timeSendingInMs = stopwatch.stop()

                        consoleOutput(
                            "${it}th write with response time $timeSendingInMs ms",
                            outputBuilder
                        )

                        totalTimeSendingInMs += timeSendingInMs
                        totalBytesSent += randomMessage.size
                        _bytesPerSecond.emit(1000f * totalBytesSent / totalTimeSendingInMs)

                        _packetsSent.emit(packetsSent.value + 1)
                    }
                    _state.emit("FINISHED $CHECK_EMOJI")
                }
            }

            TestCaseId.SR_OW_2 -> {
                gattService.startServer(BluetoothConstants.writeAckServer)
            }

            TestCaseId.SR_OW_4 -> {
                when (testRole) {
                    TestRole.A -> {
                        gattService.startServer(BluetoothConstants.writeAckServer)

                        stopwatch.start()
                        bluetoothScanner.startScan(WAKE_SERVICE_UUID)
                        val targetDevice = bluetoothScanner.scannedDeviceEvent.first()
                        consoleOutput("device discovery time ${stopwatch.stop()} ms", outputBuilder)

                        stopwatch.start()
                        bluetoothClientService.connect(targetDevice.id)

                        val connectedDevice = bluetoothClientService.deviceConnectionEvent.first()
                        consoleOutput(
                            "device connection time ${stopwatch.stop()} ms",
                            outputBuilder
                        )

                        stopwatch.start()
                        connectedDevice.discoverServices()
                        consoleOutput("service discovery time ${stopwatch.stop()} ms", outputBuilder)

                        stopwatch.start()
                        connectedDevice.writeWithResponse(
                            WAKE_SERVICE_UUID,
                            WAKE_CHARACTERISTIC_UUID,
                            "WAKE".encodeToByteArray()
                        )
                        consoleOutput("send wake time ${stopwatch.stop()} ms", outputBuilder)
                        connectedDevice.close()
                    }

                    TestRole.B -> {
                        gattService.startServer(BluetoothConstants.wakeAckServer)
                    }

                    else -> throw RuntimeException("Invalid role for test")
                }
            }
        }
        consoleOutput = outputBuilder.toString()
    }
}
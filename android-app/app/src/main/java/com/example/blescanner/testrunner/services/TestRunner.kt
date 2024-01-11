package com.example.blescanner.testrunner.services

import android.annotation.SuppressLint
import android.util.Log
import com.example.blescanner.advertiser.BluetoothConstants
import com.example.blescanner.advertiser.BluetoothConstants.RELAY_SERVICE_UUID
import com.example.blescanner.advertiser.BluetoothConstants.RELAY_WRITE_CHARACTERISTIC_UUID
import com.example.blescanner.advertiser.BluetoothConstants.WAKE_CHARACTERISTIC_UUID
import com.example.blescanner.advertiser.BluetoothConstants.WAKE_SERVICE_UUID
import com.example.blescanner.advertiser.BluetoothConstants.WRITE_CHARACTERISTIC_UUID
import com.example.blescanner.advertiser.BluetoothConstants.WRITE_SERVICE_UUID
import com.example.blescanner.advertiser.BluetoothGattService
import com.example.blescanner.advertiser.GattService
import com.example.blescanner.measurements.Stopwatch
import com.example.blescanner.model.BluetoothSession
import com.example.blescanner.scanner.service.BluetoothClientService
import com.example.blescanner.scanner.service.BluetoothScanner
import com.example.blescanner.testrunner.model.TestCaseId
import com.example.blescanner.testrunner.model.TestRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.random.Random

class TestRunner(
    private val session: BluetoothSession?,
    private val stopwatch: Stopwatch,
    private val testCase: TestCaseId,
    private val testRole: TestRole,
    private val testNodeIndex: UByte,
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
                        "mtu $mtu bytes, request time ${stopwatch.stop()} ms", outputBuilder
                    )
                    sendRandomData(session, outputBuilder, mtu)
                    _state.emit("FINISHED $CHECK_EMOJI")
                }
            }

            TestCaseId.SR_OW_2 -> {
                gattService.startServer(BluetoothConstants.writeAckServer)
            }

            TestCaseId.SR_OW_4 -> {
                when (testRole) {
                    TestRole.A -> {
                        consoleOutput("FOREGROUND", outputBuilder)
                        gattService.startServer(BluetoothConstants.writeAckServer)

                        stopwatch.start()
                        consoleOutput("Scanning for device with wake service...", outputBuilder)
                        bluetoothScanner.startScan(WAKE_SERVICE_UUID)
                        val targetDevice = bluetoothScanner.scannedDeviceEvent.first()
                        consoleOutput("device discovery time ${stopwatch.stop()} ms", outputBuilder)
                        bluetoothScanner.stopScan()

                        stopwatch.start()
                        bluetoothClientService.connect(targetDevice.id)

                        val connectedDevice = bluetoothClientService.deviceConnectionEvent.first()
                        consoleOutput(
                            "device connection time ${stopwatch.stop()} ms", outputBuilder
                        )

                        stopwatch.start()
                        connectedDevice.discoverServices()
                        consoleOutput(
                            "service discovery time ${stopwatch.stop()} ms", outputBuilder
                        )

                        stopwatch.start()
                        connectedDevice.writeWithResponse(
                            WAKE_SERVICE_UUID, WAKE_CHARACTERISTIC_UUID, "WAKE".encodeToByteArray()
                        )
                        consoleOutput("send wake time ${stopwatch.stop()} ms", outputBuilder)
                    }

                    TestRole.B -> {
                        consoleOutput("BACKGROUND", outputBuilder)
                        gattService.startServer(GattService.createWakeService { _, _, _ ->
                            stopwatch.start()
                            consoleOutput(
                                "Scanning for device with write service...", outputBuilder
                            )
                            bluetoothScanner.startScan(WRITE_SERVICE_UUID)
                            val targetDevice = bluetoothScanner.scannedDeviceEvent.first()
                            consoleOutput(
                                "device discovery time ${stopwatch.stop()} ms", outputBuilder
                            )
                            bluetoothScanner.stopScan()

                            stopwatch.start()
                            bluetoothClientService.connect(targetDevice.id)

                            val connectedDevice =
                                bluetoothClientService.deviceConnectionEvent.first()
                            consoleOutput(
                                "device connection time ${stopwatch.stop()} ms", outputBuilder
                            )

                            stopwatch.start()
                            connectedDevice.discoverServices()
                            consoleOutput(
                                "service discovery time ${stopwatch.stop()} ms", outputBuilder
                            )

                            stopwatch.start()
                            val mtu = connectedDevice.requestMtu(BluetoothSession.MAX_ATT_MTU) - 3
                            _mtu.emit(mtu)
                            consoleOutput(
                                "mtu $mtu bytes, request time ${stopwatch.stop()} ms", outputBuilder
                            )
                            sendRandomData(connectedDevice, outputBuilder, mtu)

                            connectedDevice.close()
                        })
                    }

                    else -> throw RuntimeException("Invalid role for test")
                }
            }

            TestCaseId.SR_OW_5 -> {
                // do cool stuff
                consoleOutput("I'm role $testRole with node index $testNodeIndex", outputBuilder)

                when (testRole) {
                    TestRole.A -> {
                        consoleOutput("SENDER", outputBuilder)
                        stopwatch.start()
                        consoleOutput(
                            "Scanning for device with relay service...", outputBuilder
                        )
                        bluetoothScanner.startScan(RELAY_SERVICE_UUID)
                        val targetDevice = bluetoothScanner.scannedDeviceEvent.first()
                        consoleOutput(
                            "device discovery time ${stopwatch.stop()} ms", outputBuilder
                        )
                        bluetoothScanner.stopScan()

                        stopwatch.start()
                        bluetoothClientService.connect(targetDevice.id)

                        val connectedDevice = bluetoothClientService.deviceConnectionEvent.first()
                        consoleOutput(
                            "device connection time ${stopwatch.stop()} ms", outputBuilder
                        )

                        stopwatch.start()
                        connectedDevice.discoverServices()
                        consoleOutput(
                            "service discovery time ${stopwatch.stop()} ms", outputBuilder
                        )

                        stopwatch.start()
                        val mtu = connectedDevice.requestMtu(BluetoothSession.MAX_ATT_MTU) - 3
                        _mtu.emit(mtu)
                        consoleOutput(
                            "mtu $mtu bytes, request time ${stopwatch.stop()} ms", outputBuilder
                        )
                        sendRandomData(
                            connectedDevice,
                            outputBuilder,
                            23,
                            RELAY_SERVICE_UUID,
                            RELAY_WRITE_CHARACTERISTIC_UUID,
                            1
                        )

                        connectedDevice.close()
                    }

                    TestRole.B -> {
                        consoleOutput("RELAY", outputBuilder)

                        var relayCount = 0

                        val relayService =
                            GattService.createRelayService(testNodeIndex) { _, _, value ->
                                stopwatch.start()
                                consoleOutput(
                                    "Scanning for device with relay service...", outputBuilder
                                )
                                val targetRelayServiceId = GattService.getRelayServiceIdWithNodeIndex(
                                    nodeIndex = testNodeIndex.inc()

                                )
                                bluetoothScanner.startScan(targetRelayServiceId)
                                val targetDevice = bluetoothScanner.scannedDeviceEvent.first()
                                consoleOutput(
                                    "device discovery time ${stopwatch.stop()} ms", outputBuilder
                                )
                                bluetoothScanner.stopScan()
                                stopwatch.start()
                                bluetoothClientService.connect(targetDevice.id)

                                val connectedDevice =
                                    bluetoothClientService.deviceConnectionEvent.first()
                                consoleOutput(
                                    "device connection time ${stopwatch.stop()} ms", outputBuilder
                                )

                                stopwatch.start()
                                connectedDevice.discoverServices()
                                consoleOutput(
                                    "service discovery time ${stopwatch.stop()} ms", outputBuilder
                                )

                                stopwatch.start()
                                val mtu =
                                    connectedDevice.requestMtu(BluetoothSession.MAX_ATT_MTU) - 3
                                _mtu.emit(mtu)
                                consoleOutput(
                                    "mtu $mtu bytes, request time ${stopwatch.stop()} ms",
                                    outputBuilder
                                )

                                stopwatch.start()
                                sendData(
                                    connectedDevice,
                                    targetRelayServiceId,
                                    RELAY_WRITE_CHARACTERISTIC_UUID,
                                    value
                                )
                                consoleOutput(
                                    "${relayCount}th relay write with response time ${stopwatch.stop()} ms",
                                    outputBuilder
                                )
                                consoleOutput(
                                    value.toString(StandardCharsets.UTF_8),
                                    outputBuilder
                                )
                                relayCount++

                                connectedDevice.close()
                            }
                        consoleOutput("Starting service ${relayService.serviceUUID}", outputBuilder)
                        gattService.startServer(relayService)
                    }

                    TestRole.C -> {
                        consoleOutput("RECEIVER", outputBuilder)

                        gattService.startServer(GattService.createRelayService(testNodeIndex) { _, _, value ->
                            consoleOutput(value.toString(StandardCharsets.UTF_8), outputBuilder)
                        })
                    }
                }
            }
        }
        consoleOutput = outputBuilder.toString()
    }


    private suspend fun sendRandomData(
        session: BluetoothSession,
        outputBuilder: StringBuilder,
        packetSize: Int,
        writeService: UUID = WRITE_SERVICE_UUID,
        writeCharacteristic: UUID = WRITE_CHARACTERISTIC_UUID,
        numberMessages: Int = 100
    ) {
        var totalTimeSendingInMs = 0L
        var totalBytesSent = 0
        repeat(numberMessages) {
            val indexBytes = it.toString().toByteArray(StandardCharsets.UTF_8)
            val randomMessage = randomArray(packetSize - indexBytes.size)
            stopwatch.start()
            sendData(session, writeService, writeCharacteristic, indexBytes + randomMessage)
            val timeSendingInMs = stopwatch.stop()

            consoleOutput(
                "${it}th write with response time $timeSendingInMs ms", outputBuilder
            )

            totalTimeSendingInMs += timeSendingInMs
            totalBytesSent += randomMessage.size
            _bytesPerSecond.emit(1000f * totalBytesSent / totalTimeSendingInMs)

            _packetsSent.emit(packetsSent.value + 1)
        }
    }

    private suspend fun sendData(
        session: BluetoothSession,
        writeService: UUID,
        writeCharacteristic: UUID,
        message: ByteArray
    ) {
        session.writeWithResponse(writeService, writeCharacteristic, message)
    }
}
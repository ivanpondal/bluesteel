package com.example.blescanner.testrunner.services

import android.util.Log
import com.example.blescanner.measurements.Stopwatch
import com.example.blescanner.model.BluetoothSession
import com.example.blescanner.scanner.service.BluetoothConstants.CHARACTERISTIC_UUID
import com.example.blescanner.scanner.service.BluetoothConstants.SERVICE_UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class TestRunner(private val session: BluetoothSession, private val stopwatch: Stopwatch) {

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

    suspend fun run() {
        val outputBuilder = StringBuilder()

        stopwatch.start()
        session.discoverServices()

        consoleOutput("service discovery time ${stopwatch.stop()} ms", outputBuilder)

        stopwatch.start()
        val mtu = session.requestMtu(BluetoothSession.MAX_ATT_MTU) - 3
        consoleOutput("mtu $mtu bytes, request time ${stopwatch.stop()} ms", outputBuilder)
        var totalTimeSendingInMs = 0L
        var totalBytesSent = 0
        repeat(100) {
            val randomMessage = randomArray(mtu)
            stopwatch.start()
            session.writeWithResponse(SERVICE_UUID, CHARACTERISTIC_UUID, randomMessage)
            val timeSendingInMs = stopwatch.stop()

            consoleOutput("${it}th write with response time $timeSendingInMs ms", outputBuilder)

            totalTimeSendingInMs += timeSendingInMs
            totalBytesSent += randomMessage.size
            _bytesPerSecond.emit(1000f * totalBytesSent / totalTimeSendingInMs)

            _packetsSent.emit(packetsSent.value + 1)
        }
        _state.emit("FINISHED $CHECK_EMOJI")

        consoleOutput = outputBuilder.toString()
    }
}
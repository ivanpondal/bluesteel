package com.example.blescanner.testrunner.services

import android.util.Log
import com.example.blescanner.BluetoothAdvertiserViewModel.Companion.CHARACTERISTIC_UUID
import com.example.blescanner.BluetoothAdvertiserViewModel.Companion.SERVICE_UUID
import com.example.blescanner.measurements.Stopwatch
import com.example.blescanner.model.BluetoothSession
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

    private fun randomArray(size: Int): ByteArray {
        val randomMessage = ByteArray(size)
        Random.Default.nextBytes(randomMessage)
        return randomMessage
    }

    suspend fun run() {
        stopwatch.start()
        session.discoverServices()
        Log.i(TAG, "service discovery time ${stopwatch.stop()} ms")

        stopwatch.start()
        val mtu = session.requestMtu(BluetoothSession.MAX_ATT_MTU) - 3
        Log.i(TAG, "mtu $mtu bytes, request time ${stopwatch.stop()} ms")
        repeat(100) {
            val randomMessage = randomArray(mtu)
            stopwatch.start()
            session.writeWithResponse(SERVICE_UUID, CHARACTERISTIC_UUID, randomMessage)
            Log.i(TAG, "${it}th write with response time ${stopwatch.stop()} ms")
            _packetsSent.emit(packetsSent.value + 1)
        }
        _state.emit("FINISHED $CHECK_EMOJI")
    }
}
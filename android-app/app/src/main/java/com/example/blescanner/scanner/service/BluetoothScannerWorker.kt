package com.example.blescanner.scanner.service

import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.blescanner.R
import com.example.blescanner.advertiser.BluetoothServer
import com.example.blescanner.advertiser.GattService
import com.example.blescanner.testrunner.services.TestRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class BluetoothScannerWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    companion object {
        private val TAG = TestRunner::class.simpleName
    }

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())
        Log.i(TAG, "Running bluetooth scanner worker")

        val bluetoothManager =
            this.applicationContext.getSystemService(ComponentActivity.BLUETOOTH_SERVICE) as BluetoothManager

        val bluetoothServer = BluetoothServer(
            bluetoothManager, this.applicationContext,
            CoroutineScope(Dispatchers.IO)
        )

        val gattService =
            GattService(BluetoothConstants.SERVICE_UUID, BluetoothConstants.CHARACTERISTIC_UUID)

        Log.i(TAG, "Publishing service")
        bluetoothServer.publishService(gattService)

        Log.i(TAG, "Advertising service")
        bluetoothServer.startAdvertising(gattService)

        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, "channelId")
            .setContentTitle("GATT Server")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setTicker("GATT Server - ticker")
            .setContentText("Running...")
            .setOngoing(true)
            .build()

        return ForegroundInfo(42, notification)
    }

}
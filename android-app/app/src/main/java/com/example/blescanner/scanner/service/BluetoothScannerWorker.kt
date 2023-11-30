package com.example.blescanner.scanner.service

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.blescanner.testrunner.services.TestRunner

class BluetoothScannerWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    companion object {
        private val TAG = TestRunner::class.simpleName
    }
    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())

        Log.i(TAG, "Running bluetooth scanner worker")
        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext,"channelId" )
            .setContentTitle("GATT Server")
            .setTicker("GATT Server - ticker")
            .setContentText("Running...")
            .setOngoing(true)
            .build()

        return ForegroundInfo(42, notification)
    }
}
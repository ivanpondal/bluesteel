package com.example.blescanner.advertiser

import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.blescanner.R
import com.example.blescanner.scanner.service.BluetoothConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothGattService() : Service() {

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val bluetoothManager: BluetoothManager by lazy {
        application.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    }

    companion object {
        private val TAG = BluetoothGattService::class.simpleName
    }

    private val binder = BluetoothGattBinder()

    inner class BluetoothGattBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): BluetoothGattService = this@BluetoothGattService
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i(TAG, "Starting bound service")
        val notification = NotificationCompat.Builder(applicationContext, "channelId")
            .setContentTitle("GATT bound Server")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setTicker("GATT Server - ticker")
            .setContentText("Running...")
            .setOngoing(true)
            .build()
        startForeground(42, notification)

        val bluetoothServer = BluetoothServer(
            bluetoothManager, this.applicationContext,
            CoroutineScope(Dispatchers.IO)
        )

        val gattService =
            GattService(BluetoothConstants.SERVICE_UUID, BluetoothConstants.CHARACTERISTIC_UUID)

        coroutineScope.launch {
            Log.i(TAG, "Publishing service")
            bluetoothServer.publishService(gattService)

            Log.i(TAG, "Advertising service")
            bluetoothServer.startAdvertising(gattService)
        }
        return binder
    }
}
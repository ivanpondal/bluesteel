package com.example.blescanner.advertiser

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.blescanner.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothGattService() : Service() {

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val bluetoothServer by lazy {
        val bluetoothManager = application.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        BluetoothServer(
            bluetoothManager, this.applicationContext,
            coroutineScope
        )
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            val name = "BleScanner channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("channelId", name, importance)
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "channelId")
            .setContentTitle("GATT bound Server")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setTicker("GATT Server - ticker")
            .setContentText("Running...")
            .setOngoing(true)
            .build()
        startForeground(42, notification)

        return binder
    }

    fun startServer(gattService: GattService) {
        coroutineScope.launch {
            Log.i(TAG, "Publishing service")
            bluetoothServer.publishService(gattService)

            Log.i(TAG, "Advertising service")
            bluetoothServer.startAdvertising(gattService)
        }
    }
}
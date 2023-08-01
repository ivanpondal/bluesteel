package com.example.blescanner.model

import android.bluetooth.BluetoothGatt

class BluetoothSession(private val bluetoothGatt: BluetoothGatt) : Identifiable<String> {
    override val id: String
        get() = bluetoothGatt.device.address
}
package com.example.blescanner.model

import android.os.ParcelUuid
import java.util.UUID

data class BluetoothScannedDevice(
    val id: String,
    val rssi: Int,
    val name: String? = null,
    val advertisements: List<ParcelUuid>
) {
}

object BluetoothDeviceData {
    val sampleDevices = listOf(
        BluetoothScannedDevice(
            id = "AD42A639-E566-46E7-B93B-13B87F29649B",
            rssi = -30,
            name = "JBL speaker",
            advertisements = emptyList()
        ),
        BluetoothScannedDevice(
            id = "7DA43699-7332-4343-A046-E6B139745102",
            rssi = -79,
            advertisements = listOf(ParcelUuid(UUID.randomUUID()))
        )
    )
}
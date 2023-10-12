package com.example.blescanner.model

import android.os.ParcelUuid
import java.util.UUID

data class BluetoothDeviceAdvertisement(
    override val id: String,
    val rssi: Int,
    val name: String? = null,
    val services: List<ParcelUuid>
) : Identifiable<String> {
}

object BluetoothDeviceData {
    val sampleDevices = listOf(
        BluetoothDeviceAdvertisement(
            id = "AD42A639-E566-46E7-B93B-13B87F29649B",
            rssi = -30,
            name = "JBL speaker",
            services = emptyList()
        ),
        BluetoothDeviceAdvertisement(
            id = "7DA43699-7332-4343-A046-E6B139745102",
            rssi = -79,
            services = listOf(ParcelUuid(UUID.randomUUID()))
        )
    )
}
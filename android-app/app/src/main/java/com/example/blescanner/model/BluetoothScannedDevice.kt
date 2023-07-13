package com.example.blescanner.model

import android.os.ParcelUuid
import java.util.Objects
import java.util.UUID

data class BluetoothScannedDevice(
    val id: String,
    val rssi: Int,
    val name: String? = null,
    val advertisements: List<ParcelUuid>
) {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is BluetoothScannedDevice) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }
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
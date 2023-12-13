package com.example.blescanner.scanner.service

import com.example.blescanner.advertiser.GattService
import java.util.UUID

object BluetoothConstants {
    val WRITE_SERVICE_UUID = UUID.fromString("FE4B1073-17BB-4982-955F-28702F277F19")
    val WRITE_CHARACTERISTIC_UUID = UUID.fromString("A5C46D55-280D-4B9E-8335-BCA4C0977BDB")

    val writeAckServer = GattService(
        WRITE_SERVICE_UUID,
        WRITE_CHARACTERISTIC_UUID
    ) { _, _, _ -> }

    val WAKE_SERVICE_UUID = UUID.fromString("935F797A-11F2-4E45-9D65-9B8D508F005A")
    val WAKE_CHARACTERISTIC_UUID = UUID.fromString("310B5E4A-747A-439F-8FA2-6C0088F53090")

    val wakeAckServer = GattService(
        WAKE_SERVICE_UUID,
        WAKE_CHARACTERISTIC_UUID
    ) { _, _, _ -> }
}
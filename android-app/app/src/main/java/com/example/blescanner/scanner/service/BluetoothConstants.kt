package com.example.blescanner.scanner.service

import com.example.blescanner.advertiser.GattService
import java.util.UUID

object BluetoothConstants {
    val WRITE_SERVICE_UUID = UUID.fromString("7B442D4E-8D78-4214-97B3-3B2969709D69")
    val WRITE_CHARACTERISTIC_UUID: UUID = UUID.fromString("D0C253CA-07A9-47B2-BB7A-F877A56BE43B")

    val writeAckServer = GattService(
        WRITE_SERVICE_UUID,
        WRITE_CHARACTERISTIC_UUID
    ) { _, _, _ -> }

    val WAKE_SERVICE_UUID: UUID = UUID.fromString("935F797A-11F2-4E45-9D65-9B8D508F005A")
    val WAKE_CHARACTERISTIC_UUID: UUID = UUID.fromString("310B5E4A-747A-439F-8FA2-6C0088F53090")

    val wakeAckServer = GattService(
        WAKE_SERVICE_UUID,
        WAKE_CHARACTERISTIC_UUID
    ) { _, _, _ -> }
}
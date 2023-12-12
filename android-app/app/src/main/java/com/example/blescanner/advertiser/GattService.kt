package com.example.blescanner.advertiser

import java.util.UUID

class GattService(
    val serviceUUID: UUID,
    val characteristicUUID: UUID,
    val writeHandler: (deviceAddress: String, offset: Int, value: ByteArray) -> Unit
) {
}
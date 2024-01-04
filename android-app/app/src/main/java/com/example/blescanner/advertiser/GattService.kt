package com.example.blescanner.advertiser

import com.example.blescanner.scanner.service.BluetoothConstants
import java.util.UUID

class GattService(

    val serviceUUID: UUID,
    val characteristicUUID: UUID,
    val writeHandler: suspend (deviceAddress: String, offset: Int, value: ByteArray) -> Unit
) {
    companion object {
        public fun createWakeService(writeHandler: suspend (deviceAddress: String, offset: Int, value: ByteArray) -> Unit): GattService {
            return GattService(
                BluetoothConstants.WAKE_SERVICE_UUID,
                BluetoothConstants.WAKE_CHARACTERISTIC_UUID,
                writeHandler
            )
        }
    }
}

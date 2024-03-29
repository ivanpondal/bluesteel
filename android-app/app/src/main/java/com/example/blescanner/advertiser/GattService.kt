package com.example.blescanner.advertiser

import java.util.UUID

class GattService(

    val serviceUUID: UUID,
    val characteristicUUID: UUID,
    val writeHandler: suspend (deviceAddress: String, offset: Int, value: ByteArray) -> Unit
) {
    companion object {
        fun createWakeService(writeHandler: suspend (deviceAddress: String, offset: Int, value: ByteArray) -> Unit): GattService {
            return GattService(
                BluetoothConstants.WAKE_SERVICE_UUID,
                BluetoothConstants.WAKE_CHARACTERISTIC_UUID,
                writeHandler
            )
        }

        private fun incrementLastDigit(uuid: UUID, offset: UByte): UUID {
            val uuidStr = uuid.toString()
            val lastDigit = Character.getNumericValue(uuidStr.last()).toUByte()
            val newLastDigit = (lastDigit + offset) % 10u
            val newUuidStr = uuidStr.substring(0, uuidStr.length - 1) + newLastDigit
            return UUID.fromString(newUuidStr)
        }

        fun getRelayServiceIdWithNodeIndex(nodeIndex: UByte): UUID {
            return incrementLastDigit(BluetoothConstants.RELAY_SERVICE_UUID, nodeIndex)
        }

        fun createRelayService(
            nodeIndex: UByte,
            writeHandler: suspend (deviceAddress: String, offset: Int, value: ByteArray) -> Unit
        ): GattService {
            return GattService(
                getRelayServiceIdWithNodeIndex(nodeIndex),
                BluetoothConstants.RELAY_WRITE_CHARACTERISTIC_UUID,
                writeHandler
            )
        }
    }

}

//
//  TestRunner.swift
//  BLEScanner
//
//  Created by iván pondal on 11/05/2023.
//

import Foundation

class TestRunner {
    let bluetoothRadio: BluetoothRadio
    let testCase: TestCase
    let device: Device

    init(bluetoothRadio: BluetoothRadio, testCase: TestCase, device: Device) {
        self.bluetoothRadio = bluetoothRadio
        self.testCase = testCase
        self.device = device
    }

    func run() async {
        do {
            let _ = try await bluetoothRadio.discover(fromPeripheralWithId: device.id, serviceId: BluetoothRadio.serviceUUID)

            let peripheralWithCharacteristic = try await bluetoothRadio.discover(fromPeripheralWithId: device.id, serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID)

            guard let service = peripheralWithCharacteristic.services?.first(where: {$0.uuid == BluetoothRadio.serviceUUID}) else { return }
            guard let firstCharacteristic = service.characteristics?.first else { return }

            guard let data = "HOLA MUNDO xd".data(using: .utf8) else { return }

            peripheralWithCharacteristic.writeValue(data, for: firstCharacteristic, type: .withoutResponse)
        } catch {
            print("\(error)")
        }
    }
}


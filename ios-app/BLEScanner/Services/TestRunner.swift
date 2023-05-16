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
    let stopwatch: Stopwatch

    init(bluetoothRadio: BluetoothRadio, testCase: TestCase, device: Device) {
        self.bluetoothRadio = bluetoothRadio
        self.testCase = testCase
        self.device = device
        self.stopwatch = Stopwatch()
    }

    func run() async {
        do {

            stopwatch.start()
            let _ = try await bluetoothRadio.discover(fromPeripheralWithId: device.id, serviceId: BluetoothRadio.serviceUUID)
            print("service discovery time \(stopwatch.stop().formatted(.units(allowed: [.microseconds])))")

            stopwatch.start()
            let _ = try await bluetoothRadio.discover(fromPeripheralWithId: device.id, serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID)
            print("characteristic discovery time \(stopwatch.stop().formatted(.units(allowed: [.microseconds])))")

            guard let data = "HOLA MUNDO xd".data(using: .utf8) else { return }

            stopwatch.start()
            let _ = try await bluetoothRadio.writeWithResponse(toPeripheralWithId: device.id, serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID, data: data)
            print("write with response time \(stopwatch.stop().formatted(.units(allowed: [.microseconds])))")
        } catch {
            print("\(error)")
        }
    }
}


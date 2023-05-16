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

    private func generateRandomBytes(count: Int) -> Data {
        return Data((0 ..< count).map( { _ in  UInt8.random(in: UInt8.min ... UInt8.max)}))
    }

    func run() async {
        do {
            if (testCase.id == TestCaseId.SR_OW_1){
                let mtu = try bluetoothRadio.mtu(forPeripheralId: device.id, withWriteType: .withResponse)
                print("mtu \(mtu) bytes")

                stopwatch.start()
                let _ = try await bluetoothRadio.discover(fromPeripheralWithId: device.id, serviceId: BluetoothRadio.serviceUUID)
                print("service discovery time \(stopwatch.stop().formatted(.units(allowed: [.microseconds])))")

                stopwatch.start()
                let _ = try await bluetoothRadio.discover(fromPeripheralWithId: device.id, serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID)
                print("characteristic discovery time \(stopwatch.stop().formatted(.units(allowed: [.microseconds])))")

                for i in 0..<100 {
                    let data = generateRandomBytes(count: mtu)

                    stopwatch.start()
                    let _ = try await bluetoothRadio.writeWithResponse(toPeripheralWithId: device.id, serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID, data: data)
                    print("\(i)th write with response time \(stopwatch.stop().formatted(.units(allowed: [.microseconds])))")
                }
            }
        } catch {
            print("\(error)")
        }
    }
}


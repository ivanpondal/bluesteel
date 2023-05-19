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
    var stopwatch: Stopwatch

    @Published
    var state: String = "RUNNING"

    init(bluetoothRadio: BluetoothRadio, testCase: TestCase, device: Device) {
        self.bluetoothRadio = bluetoothRadio
        self.testCase = testCase
        self.device = device
        if #available(iOS 16, *) {
            self.stopwatch = ContinuousClockStopwatch()
        } else {
            self.stopwatch = DateStopwatch()
        }
    }

    private func generateRandomBytes(count: Int) -> Data {
        return Data((0 ..< count).map( { _ in  UInt8.random(in: UInt8.min ... UInt8.max)}))
    }

    func run() async {
        do {
            if (testCase.id == TestCaseId.SR_OW_1){
                stopwatch.start()
                let _ = try await bluetoothRadio.discover(fromPeripheralWithId: device.id, serviceId: BluetoothRadio.serviceUUID)
                print("service discovery time \(stopwatch.stop()) ms")

                stopwatch.start()
                let _ = try await bluetoothRadio.discover(fromPeripheralWithId: device.id, serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID)
                print("characteristic discovery time \(stopwatch.stop()) ms")

                let mtu = try bluetoothRadio.mtu(forPeripheralId: device.id, withWriteType: .withoutResponse)
                print("mtu \(mtu) bytes")

                for i in 0..<100 {
                    stopwatch.start()
                    let data = generateRandomBytes(count: mtu)
                    print("\(i)th random bytes generation time \(stopwatch.stop()) ms")

                    stopwatch.start()
                    let _ = try await bluetoothRadio.writeWithResponse(toPeripheralWithId: device.id, serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID, data: data)
                    print("\(i)th write with response time \(stopwatch.stop()) ms")
                }
                state = "FINISHED"
            }
        } catch {
            print("something went wrong: \(error)")
        }
    }
}


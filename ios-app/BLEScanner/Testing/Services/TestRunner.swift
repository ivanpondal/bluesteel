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
    let targetDevice: Device?
    var stopwatch: Stopwatch

    private var totalBytesSent: Int = 0
    private var totalTimeSendingInMs: Int64 = 0

    @Published
    var state: String = "RUNNING 🏃‍♂️"
    @Published
    var packetsSent: Int = 0
    @Published
    var bytesSentPerSecond: Float = 0
    @Published
    var mtu: Int = 0

    var consoleOutput: String = ""
    private static let dateFormatter: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions.insert(.withFractionalSeconds)
        return formatter
    }()

    init(bluetoothRadio: BluetoothRadio, testCase: TestCase, targetDevice: Device?) {
        self.bluetoothRadio = bluetoothRadio
        self.testCase = testCase
        self.targetDevice = targetDevice
        if #available(iOS 16, *) {
            self.stopwatch = ContinuousClockStopwatch()
        } else {
            self.stopwatch = DateStopwatch()
        }
    }

    private func generateRandomBytes(count: Int) -> Data {
        return Data((0 ..< count).map( { _ in  UInt8.random(in: UInt8.min ... UInt8.max)}))
    }

    private func console(print text: String) -> Void {
        print(text)
        let now = TestRunner.dateFormatter.string(from: Date.now)
        consoleOutput += "\(now) - \(text)\n"
    }

    func run() async {
        do {
            switch testCase.id {
            case .SR_OW_1:
                guard let targetDevice = targetDevice else {
                    fatalError("no target device found")
                }
                stopwatch.start()
                let _ = try await bluetoothRadio.discover(fromPeripheralWithId: targetDevice.id, serviceId: BluetoothRadio.serviceUUID)
                console(print: "service discovery time \(stopwatch.stop()) ms")

                stopwatch.start()
                let _ = try await bluetoothRadio.discover(fromPeripheralWithId: targetDevice.id, serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID)
                console(print: "characteristic discovery time \(stopwatch.stop()) ms")

                mtu = try bluetoothRadio.mtu(forPeripheralId: targetDevice.id, withWriteType: .withoutResponse)
                let mtuWithResponse = try bluetoothRadio.mtu(forPeripheralId: targetDevice.id, withWriteType: .withResponse)
                console(print: "mtu \(mtu) bytes without response")
                console(print: "mtu \(mtuWithResponse) bytes with response")

                for i in 0..<100 {
                    stopwatch.start()
                    let data = generateRandomBytes(count: mtu)
                    console(print: "\(i)th random bytes generation time \(stopwatch.stop()) ms")

                    stopwatch.start()
                    let _ = try await bluetoothRadio.writeWithResponse(toPeripheralWithId: targetDevice.id, serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID, data: data)
                    let sendTimeInMs = stopwatch.stop()
                    totalTimeSendingInMs += sendTimeInMs
                    totalBytesSent += data.count
                    bytesSentPerSecond = Float(1000 * totalBytesSent)/Float(totalTimeSendingInMs)
                    packetsSent += 1

                    console(print: "\(i)th write with response time \(sendTimeInMs) ms")
                }
                state = "FINISHED ☑️"
            case .SR_OW_2:
                stopwatch.start()
                let targetPeripheral = await bluetoothRadio.discover(peripheralWithService: BluetoothRadio.serviceUUID)
                console(print: "target device discovery time \(stopwatch.stop()) ms")

                stopwatch.start()
                let connectedPeripheral = await bluetoothRadio.connect(toPeripheral: targetPeripheral)
                console(print: "target device discovery time \(stopwatch.stop()) ms")

                stopwatch.start()
                let _ = try await bluetoothRadio.discover(fromPeripheralWithId: connectedPeripheral.identifier, serviceId: BluetoothRadio.serviceUUID)
                console(print: "service discovery time \(stopwatch.stop()) ms")

                stopwatch.start()
                let _ = try await bluetoothRadio.discover(fromPeripheralWithId: connectedPeripheral.identifier, serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID)
                console(print: "characteristic discovery time \(stopwatch.stop()) ms")

                mtu = try bluetoothRadio.mtu(forPeripheralId: connectedPeripheral.identifier, withWriteType: .withoutResponse)
                let mtuWithResponse = try bluetoothRadio.mtu(forPeripheralId: connectedPeripheral.identifier, withWriteType: .withResponse)
                console(print: "mtu \(mtu) bytes without response")
                console(print: "mtu \(mtuWithResponse) bytes with response")

                for i in 0..<100 {
                    stopwatch.start()
                    let data = generateRandomBytes(count: mtu)
                    console(print: "\(i)th random bytes generation time \(stopwatch.stop()) ms")

                    stopwatch.start()
                    let _ = try await bluetoothRadio.writeWithResponse(toPeripheralWithId: connectedPeripheral.identifier, serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID, data: data)
                    let sendTimeInMs = stopwatch.stop()
                    totalTimeSendingInMs += sendTimeInMs
                    totalBytesSent += data.count
                    bytesSentPerSecond = Float(1000 * totalBytesSent)/Float(totalTimeSendingInMs)
                    packetsSent += 1

                    console(print: "\(i)th write with response time \(sendTimeInMs) ms")
                }

                bluetoothRadio.disconnect(fromPeripheral: connectedPeripheral)
                state = "FINISHED ☑️"
            }
        } catch {
            console(print: "something went wrong: \(error)")
            state = "FINISHED ☑️"
        }
    }
}


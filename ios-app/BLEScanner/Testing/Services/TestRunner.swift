//
//  TestRunner.swift
//  BLEScanner
//
//  Created by iván pondal on 11/05/2023.
//

import Foundation
import BackgroundTasks
import UIKit
import CoreBluetooth

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

                try await discoverWriteCharacteristic(withId: BluetoothRadio.chracteristicUUID,
                                                      fromServiceWithId: BluetoothRadio.serviceUUID, onDeviceWithId: targetDevice.id)

                try negotiateMtu(fromDeviceWithId: targetDevice.id)

                try await sendData(toDeviceWithId: targetDevice.id,
                                   serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID, packetSize: mtu)

                try bluetoothRadio.disconnect(fromPeripheralWithId: targetDevice.id)
            case .SR_OW_2:
                let targetPeripheral = try await stopwatch.measure {
                    await bluetoothRadio.discover(peripheralWithService: BluetoothRadio.serviceUUID)
                } onStop: { console(print: "target device discovery time \($0) ms") }

                let connectedPeripheral = try await stopwatch.measure {
                    try await bluetoothRadio.connect(toPeripheralWithId: targetPeripheral.identifier)
                } onStop: { console(print: "target device connection time \($0) ms") }

                try await discoverWriteCharacteristic(withId: BluetoothRadio.chracteristicUUID,
                                                      fromServiceWithId: BluetoothRadio.serviceUUID, onDeviceWithId: connectedPeripheral.identifier)

                try negotiateMtu(fromDeviceWithId: connectedPeripheral.identifier)

                try await sendData(toDeviceWithId: connectedPeripheral.identifier,
                                   serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID, packetSize: mtu)

                try bluetoothRadio.disconnect(fromPeripheralWithId: connectedPeripheral.identifier)
            case .SR_OW_3:
                let taskRequest = BGAppRefreshTaskRequest(identifier: "com.blescanner.srow3")
                taskRequest.earliestBeginDate = Date(timeIntervalSinceNow: 60)
                try BGTaskScheduler.shared.submit(taskRequest)
                console(print: "submitted test")
            case .SR_OW_4:
                switch testCase.role {
                case .A:
                    try await bluetoothRadio.publish(service: TestCase.createWriteTestService(), withLocalName: UIDevice.current.name)

                    let targetPeripheral = try await stopwatch.measure {
                        await bluetoothRadio.discover(peripheralWithService: TestCase.wakeServiceUUID)
                    } onStop: { console(print: "target device discovery time \($0) ms") }

                    let connectedPeripheral = try await stopwatch.measure {
                        try await bluetoothRadio.connect(toPeripheralWithId: targetPeripheral.identifier)
                    } onStop: { console(print: "target device connection time \($0) ms") }

                    try await discoverWriteCharacteristic(withId: TestCase.wakeCharacteristicUUID,
                                                          fromServiceWithId: TestCase.wakeServiceUUID, onDeviceWithId: connectedPeripheral.identifier)
                    try await sendWake(toDeviceWithId: connectedPeripheral.identifier)
                case .B:
                    try await bluetoothRadio.publish(service: TestCase.createWakeService { [self] in
                        Task {
                            let targetPeripheral = try await stopwatch.measure {
                                await bluetoothRadio.discover(peripheralWithService: TestCase.writeTestServiceUUID)
                            } onStop: { console(print: "target device discovery time \($0) ms") }

                            let connectedPeripheral = try await stopwatch.measure {
                                try await bluetoothRadio.connect(toPeripheralWithId: targetPeripheral.identifier)
                            } onStop: { console(print: "target device connection time \($0) ms") }

                            try await discoverWriteCharacteristic(withId: TestCase.writeTestCharacteristicUUID,
                                                                  fromServiceWithId: TestCase.writeTestServiceUUID,
                                                                  onDeviceWithId: connectedPeripheral.identifier)

                            try negotiateMtu(fromDeviceWithId: connectedPeripheral.identifier)

                            try await sendData(toDeviceWithId: connectedPeripheral.identifier,
                                               serviceId: TestCase.writeTestServiceUUID,
                                               characteristicId: TestCase.writeTestCharacteristicUUID, packetSize: mtu)

                            try bluetoothRadio.disconnect(fromPeripheralWithId: connectedPeripheral.identifier)
                        }
                    }, withLocalName: UIDevice.current.name)
                }
            case .SR_OW_5:
                switch testCase.role {
                case .A:
                    let targetPeripheral = try await stopwatch.measure {
                        await bluetoothRadio.discover(peripheralWithService: TestCase.relayServiceUUID)
                    } onStop: { console(print: "target device discovery time \($0) ms") }

                    let connectedPeripheral = try await stopwatch.measure {
                        try await bluetoothRadio.connect(toPeripheralWithId: targetPeripheral.identifier)
                    } onStop: { console(print: "target device connection time \($0) ms") }

                    try await discoverWriteCharacteristic(withId: TestCase.relayWriteCharacteristicUUID,
                                                          fromServiceWithId: TestCase.relayServiceUUID, onDeviceWithId: connectedPeripheral.identifier)

                    try negotiateMtu(fromDeviceWithId: connectedPeripheral.identifier)

                    try await sendData(toDeviceWithId: connectedPeripheral.identifier,
                                       serviceId: TestCase.relayServiceUUID, characteristicId: TestCase.relayWriteCharacteristicUUID, packetSize: mtu+10, numberMessages: 100)

                    try bluetoothRadio.disconnect(fromPeripheralWithId: connectedPeripheral.identifier)
                default:
                    console(print: "NOT IMPLEMENTED")

                }
            }
        } catch {
            console(print: "something went wrong: \(error)")
        }
        state = "FINISHED ☑️"
    }

    fileprivate func discoverWriteCharacteristic(withId characteristicId: CBUUID, fromServiceWithId serviceId: CBUUID, onDeviceWithId deviceId: UUID) async throws {
        try await stopwatch.measure {
            let _ = try await bluetoothRadio.discover(fromPeripheralWithId: deviceId, serviceId: serviceId)
        } onStop: { console(print: "service discovery time \($0) ms") }

        try await stopwatch.measure {
            let _ =  try await bluetoothRadio.discover(fromPeripheralWithId: deviceId, serviceId: serviceId, characteristicId: characteristicId)
        } onStop: { console(print: "characteristic discovery time \($0) ms") }
    }

    fileprivate func negotiateMtu(fromDeviceWithId deviceId: UUID) throws {
        mtu = try bluetoothRadio.mtu(forPeripheralId: deviceId, withWriteType: .withoutResponse)
        let mtuWithResponse = try bluetoothRadio.mtu(forPeripheralId: deviceId, withWriteType: .withResponse)
        console(print: "mtu \(mtu) bytes without response")
        console(print: "mtu \(mtuWithResponse) bytes with response")
    }

    fileprivate func sendData(toDeviceWithId deviceId: UUID, serviceId: CBUUID, characteristicId: CBUUID,
                              packetSize: Int, numberMessages: Int = 100) async throws {
        for i in 0..<numberMessages {
            guard var indexData = String(i).data(using: .utf8) else {
                return
            }
            let data = try await stopwatch.measure {
                generateRandomBytes(count: packetSize - indexData.count)
            } onStop: { console(print: "\(i)th random bytes generation time \($0) ms") }
            indexData.append(data)

            let _ = try await stopwatch.measure {
                let _ = try await bluetoothRadio.writeWithResponse(toPeripheralWithId: deviceId,
                                                                   serviceId: serviceId,
                                                                   characteristicId: characteristicId,
                                                                   data: indexData)
            } onStop: { sendTimeInMs in
                totalTimeSendingInMs += sendTimeInMs
                totalBytesSent += data.count
                bytesSentPerSecond = Float(1000 * totalBytesSent)/Float(totalTimeSendingInMs)
                packetsSent += 1

                console(print: "\(i)th write with response time \(sendTimeInMs) ms")
            }
        }
    }

    fileprivate func sendWake(toDeviceWithId deviceId: UUID) async throws {
        let _ = try await stopwatch.measure {
            let _ = try await bluetoothRadio.writeWithResponse(toPeripheralWithId: deviceId,
                                                               serviceId: TestCase.wakeServiceUUID,
                                                               characteristicId: TestCase.wakeCharacteristicUUID,
                                                               data: "WAKE".data(using: .utf8)!)
        } onStop: { sendTimeInMs in
            totalTimeSendingInMs += sendTimeInMs
            console(print: "wake with response time \(sendTimeInMs) ms")
        }
    }

}


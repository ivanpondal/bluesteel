//
//  TestRunner.swift
//  BLEScanner
//
//  Created by iván pondal on 11/05/2023.
//

import Foundation
import BackgroundTasks
import UIKit

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

                try await discoverWriteCharacteristic(fromDeviceWithId: targetDevice.id)

                try negotiateMtu(fromDeviceWithId: targetDevice.id)

                try await sendData(toDeviceWithId: targetDevice.id)

                try bluetoothRadio.disconnect(fromPeripheralWithId: targetDevice.id)
            case .SR_OW_2:
                let targetPeripheral = try await stopwatch.measure {
                    await bluetoothRadio.discover(peripheralWithService: BluetoothRadio.serviceUUID)
                } onStop: { console(print: "target device discovery time \($0) ms") }

                let connectedPeripheral = try await stopwatch.measure {
                    try await bluetoothRadio.connect(toPeripheralWithId: targetPeripheral.identifier)
                } onStop: { console(print: "target device connection time \($0) ms") }

                try await discoverWriteCharacteristic(fromDeviceWithId: connectedPeripheral.identifier)

                try negotiateMtu(fromDeviceWithId: connectedPeripheral.identifier)

                try await sendData(toDeviceWithId: connectedPeripheral.identifier)

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
                    // send wake after N seconds
                    // test server should stop (and mark test as done) once all writes are received or disconnection event from wake server
                    try bluetoothRadio.disconnect(fromPeripheralWithId: connectedPeripheral.identifier)
                case .B:
                    try await bluetoothRadio.publish(service: TestCase.createWakeService(), withLocalName: UIDevice.current.name)
                    // when wake is receive, search device with write test server
                    // connect to device and start write test
                    // wake server should stop once the agent has finished writing data0
                }
            }
        } catch {
            console(print: "something went wrong: \(error)")
        }
        state = "FINISHED ☑️"
    }

    fileprivate func discoverWriteCharacteristic(fromDeviceWithId deviceId: UUID) async throws {
        try await stopwatch.measure {
            let _ = try await bluetoothRadio.discover(fromPeripheralWithId: deviceId, serviceId: BluetoothRadio.serviceUUID)
        } onStop: { console(print: "service discovery time \($0) ms") }

        try await stopwatch.measure {
            let _ =  try await bluetoothRadio.discover(fromPeripheralWithId: deviceId, serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID)
        } onStop: { console(print: "characteristic discovery time \($0) ms") }
    }

    fileprivate func negotiateMtu(fromDeviceWithId deviceId: UUID) throws {
        mtu = try bluetoothRadio.mtu(forPeripheralId: deviceId, withWriteType: .withoutResponse)
        let mtuWithResponse = try bluetoothRadio.mtu(forPeripheralId: deviceId, withWriteType: .withResponse)
        console(print: "mtu \(mtu) bytes without response")
        console(print: "mtu \(mtuWithResponse) bytes with response")
    }
    
    fileprivate func sendData(toDeviceWithId deviceId: UUID) async throws {
        for i in 0..<100 {
            let data = try await stopwatch.measure {
                generateRandomBytes(count: mtu)
            } onStop: { console(print: "\(i)th random bytes generation time \($0) ms") }

            let _ = try await stopwatch.measure {
                let _ = try await bluetoothRadio.writeWithResponse(toPeripheralWithId: deviceId,
                                                                   serviceId: BluetoothRadio.serviceUUID,
                                                                   characteristicId: BluetoothRadio.chracteristicUUID,
                                                                   data: data)
            } onStop: { sendTimeInMs in
                totalTimeSendingInMs += sendTimeInMs
                totalBytesSent += data.count
                bytesSentPerSecond = Float(1000 * totalBytesSent)/Float(totalTimeSendingInMs)
                packetsSent += 1

                console(print: "\(i)th write with response time \(sendTimeInMs) ms")
            }
        }
    }

}


//
//  TestCase.swift
//  BLEScanner
//
//  Created by iván pondal on 04/05/2023.
//

import Foundation
import CoreBluetooth

enum TestCaseId : String, CaseIterable {
    case SR_OW_1
    case SR_OW_2
    case SR_OW_3
    case SR_OW_4
    case SR_OW_5

    func displayName() -> String {
        return rawValue.replacingOccurrences(of: "_", with: "-")
    }
}


enum TestCaseRole : String, CaseIterable {
    case A
    case B
}

struct TestCase : Identifiable, Equatable {
    let id : TestCaseId
    let role: TestCaseRole

}

extension TestCase {
    static let writeTestServiceUUID = CBUUID(string: "7B442D4E-8D78-4214-97B3-3B2969709D69")
    static let writeTestCharacteristicUUID = CBUUID(string: "D0C253CA-07A9-47B2-BB7A-F877A56BE43B")

    static let wakeServiceUUID = CBUUID(string: "935F797A-11F2-4E45-9D65-9B8D508F005A")
    static let wakeCharacteristicUUID = CBUUID(string: "310B5E4A-747A-439F-8FA2-6C0088F53090")

    static let relayServiceUUID = CBUUID(string: "476151B3-2A52-4E28-A985-10C7306D5DB0")
    static let relayWriteCharacteristicUUID = CBUUID(string: "FA33E13F-B248-4F10-BBE0-76698425F27C")

    static var sampleData: [TestCase] {
        [TestCase(id: .SR_OW_1, role: TestCaseRole.A)]
    }

    static func createWriteTestService() -> PeripheralService {
        return PeripheralService(serviceId: writeTestServiceUUID, characteristicId: writeTestCharacteristicUUID, writeHandler: {central, data in
                print("receive message from ", central, String(bytes: data, encoding: .ascii)!)
        })
    }

    static func createWakeService(onWake: @escaping () -> Void) -> PeripheralService {
        return PeripheralService(serviceId: wakeServiceUUID, characteristicId: wakeCharacteristicUUID, writeHandler: {central, data in
            onWake()
        })
    }
}

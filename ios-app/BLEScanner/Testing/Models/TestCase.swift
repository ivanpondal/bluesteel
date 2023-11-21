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

    static let writeTestCharacteristicUUID = CBUUID(string: "D0C253CA-07A9-47B2-BB7A-F877A56BE43B")
    static let writeTestServiceUUID = CBUUID(string: "7B442D4E-8D78-4214-97B3-3B2969709D69")

    static var sampleData: [TestCase] {
        [TestCase(id: .SR_OW_1, role: TestCaseRole.A)]
    }

    static func createWriteTestService() -> CBMutableService {
        let characteristic = CBMutableCharacteristic(type: writeTestCharacteristicUUID, properties: [.writeWithoutResponse, .write], value: nil, permissions: [.writeable])
        let service = CBMutableService(type: writeTestServiceUUID, primary: true)

        service.characteristics = [characteristic]

        return service
    }
}

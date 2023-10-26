//
//  TestCase.swift
//  BLEScanner
//
//  Created by iván pondal on 04/05/2023.
//

import Foundation

enum TestCaseId : String, CaseIterable {
    case SR_OW_1
    case SR_OW_2

    func displayName() -> String {
        return rawValue.replacingOccurrences(of: "_", with: "-")
    }
}


enum TestCaseRole : String, CaseIterable {
    case SENDER
    case RECEIVER
}

struct TestCase : Identifiable, Equatable {
    let id : TestCaseId
    let role: TestCaseRole
    let device: Device?

}

extension TestCase {
    static var sampleData: [TestCase] {
        [TestCase(id: .SR_OW_1, role: TestCaseRole.SENDER, device: Device.sampleData.first)]
    }
}

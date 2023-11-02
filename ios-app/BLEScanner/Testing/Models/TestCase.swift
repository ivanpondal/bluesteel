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
    case SR_OW_3

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

}

extension TestCase {
    static var sampleData: [TestCase] {
        [TestCase(id: .SR_OW_1, role: TestCaseRole.SENDER)]
    }
}

//
//  TestCase.swift
//  BLEScanner
//
//  Created by iván pondal on 04/05/2023.
//

import Foundation

enum TestCaseId : String, CaseIterable {
   case SR_OW_1

    func displayName() -> String {
        return rawValue.replacingOccurrences(of: "_", with: "-")
    }
}

struct TestCase : Identifiable, Equatable {
    let id : TestCaseId
    let devices: [Device]
}

extension TestCase {
    static var sampleData: [TestCase] {
        [TestCase(id: .SR_OW_1, devices: Device.sampleData)]
    }
}

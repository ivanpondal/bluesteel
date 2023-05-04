//
//  TestCase.swift
//  BLEScanner
//
//  Created by iván pondal on 04/05/2023.
//

import Foundation

struct TestCase : Identifiable, Equatable {
    let id : String
}

extension TestCase {
    static var sampleData: [TestCase] {
        [TestCase(id: "SR-OW-1")]
    }
}

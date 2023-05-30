//
//  Device.swift
//  BLEScanner
//
//  Created by iván pondal on 10/10/2022.
//

import Foundation

struct Device : Identifiable, Equatable {
    let id : UUID
    let rssi: Int
    let name : String?
    let advertisedServices: [UUID]
}

extension Device {
    static var sampleData: [Device] {
        [
            Device(id: UUID(uuidString: "367E9B05-BA41-400B-8F05-CED182FFAF5A")!, rssi: 34 , name: "Speaker", advertisedServices: [UUID(uuidString: "A0E1D276-E84F-4980-94BC-3E7CACA55053")!]),
            Device(id: UUID(uuidString: "AD81F21F-FFFB-480C-9F71-B0634E81E186")!, rssi: 54, name: "Headphones", advertisedServices: []),
            Device(id: UUID(uuidString: "A7380721-0036-4C18-8B61-25AE9F3CF44D")!, rssi: 89, name: nil, advertisedServices: [])
        ]
    }
}

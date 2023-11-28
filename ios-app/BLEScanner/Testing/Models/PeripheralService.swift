//
//  PeripheralService.swift
//  BLEScanner
//
//  Created by iván pondal on 23/11/2023.
//

import Foundation
import CoreBluetooth

struct PeripheralService {
    let serviceId: CBUUID
    let characteristicId: CBUUID
    let writeHandler: (_ central: CBCentral, _ data: Data) -> Void

    func toMutableService() -> CBMutableService {
        let characteristic = CBMutableCharacteristic(type: characteristicId, properties: [.writeWithoutResponse, .write], value: nil, permissions: [.writeable])
        let service = CBMutableService(type: serviceId, primary: true)

        service.characteristics = [characteristic]

        return service
    }
}

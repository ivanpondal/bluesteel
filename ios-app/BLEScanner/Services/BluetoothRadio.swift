//
//  BLEManager.swift
//  BLEScanner
//
//  Created by iván pondal on 19/10/2022.
//

import Foundation
import CoreBluetooth
import Combine
import UIKit

struct PeripheralAdvertisement {
    let peripheral: CBPeripheral
    let advertisements: [String: Any]
    let rssi: Int
}

class BluetoothRadio : NSObject, CBPeripheralManagerDelegate {
    var centralManager: CBCentralManager?
    var peripheralManager: CBPeripheralManager?

    var stateSubject: CurrentValueSubject<CBManagerState, Never> = .init(.unknown)
    var peripheralSubject: PassthroughSubject<PeripheralAdvertisement, Never> = .init()

    private var connectedPeripherals: Dictionary<UUID, CBPeripheral> = [:]

    var connectionEventSubject: PassthroughSubject<CBPeripheral, Never> = .init()
    var disconnectionEventSubject: PassthroughSubject<CBPeripheral, Never> = .init()

    func start() {
        centralManager = CBCentralManager(delegate: self, queue: .global(qos: .utility), options: [CBCentralManagerOptionShowPowerAlertKey: true])
        peripheralManager = CBPeripheralManager(delegate: self, queue: .global(qos: .utility), options: [CBPeripheralManagerOptionShowPowerAlertKey: true])
    }
    
    private func peripheral(withId peripheralUUID: UUID) -> CBPeripheral? {
        centralManager?.retrievePeripherals(withIdentifiers: [peripheralUUID])[0]
    }
    
    func connect(toPeripheralWithId uuid: UUID) {
        if (!connectedPeripherals.contains(where: {$0.key == uuid})){
            guard let peripheral = peripheral(withId: uuid) else { return }

            connectedPeripherals.updateValue(peripheral, forKey: uuid)

            centralManager?.connect(peripheral)
        }
    }
}


extension BluetoothRadio: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOff:
            print("poweredOff")
        case .unauthorized:
            print("unauthorized")
        case .unsupported:
            print("unsupported")
        case .unknown:
            print("unknown")
        case .resetting:
            print("resetting")
        case .poweredOn:
            print("poweredOn")
            centralManager?.scanForPeripherals(withServices: [BluetoothRadio.serviceUUID], options: [CBCentralManagerScanOptionAllowDuplicatesKey: true])
        default:
            print("other")
        }
        stateSubject.value = central.state
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        //        print("id:", peripheral.identifier, "state:", peripheral.state.rawValue, "rssi:", RSSI, "name:", peripheral.name ?? "n/a")
        
        // hide invalid rssi advertisements
        if RSSI.intValue < 0 {
            peripheralSubject.send(PeripheralAdvertisement(peripheral: peripheral, advertisements: advertisementData, rssi: RSSI.intValue))
        }
    }
    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        peripheral.delegate = self

        connectionEventSubject.send(peripheral)
        print("connected to ", peripheral)
        peripheral.discoverServices([BluetoothRadio.serviceUUID])
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        print("failed to connect to ", peripheral, error ?? "unkown error")
        connectedPeripherals.removeValue(forKey: peripheral.identifier)
    }

    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        connectedPeripherals.removeValue(forKey: peripheral.identifier)
        disconnectionEventSubject.send(peripheral)
        print("disconnected from ", peripheral, error != nil ? "cause \(String(describing: error))" : "")
    }
}

extension BluetoothRadio: CBPeripheralDelegate {
    // MARK: Peripheral server
    static let chracteristicUUID = CBUUID(string: "A5C46D55-280D-4B9E-8335-BCA4C0977BDB")
    static let serviceUUID = CBUUID(string: "FE4B1073-17BB-4982-955F-28702F277F19")
    
    fileprivate func createService() -> CBMutableService {
        let characteristic = CBMutableCharacteristic(type: BluetoothRadio.chracteristicUUID, properties: [.writeWithoutResponse], value: nil, permissions: [.writeable])
        let service = CBMutableService(type: BluetoothRadio.serviceUUID, primary: true)
        
        service.characteristics = [characteristic]
        
        return service
    }
    
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        switch peripheral.state {
        case .poweredOn:
            peripheral.add(createService())
        case .poweredOff:
            print("peripheral is down")
        default:
            print("other")
        }
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, didAdd service: CBService, error: Error?) {
        if let errorDescription = error?.localizedDescription {
            print("something went wrong adding peripheral service: ", errorDescription)
        } else {
            print("peripheral service is up")
            peripheral.startAdvertising([CBAdvertisementDataLocalNameKey: UIDevice.current.name, CBAdvertisementDataServiceUUIDsKey: [BluetoothRadio.serviceUUID]])
        }
    }
    
    func peripheralManagerDidStartAdvertising(_ peripheral: CBPeripheralManager, error: Error?) {
        if let errorDescription = error?.localizedDescription {
            print("something went wrong advertising peripheral service: ", errorDescription)
        } else {
            print("peripheral is advertising service")
        }
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveWrite requests: [CBATTRequest]) {
        let firstRequest = requests[0]
        
        for request in requests {
            guard let data = request.value else { peripheral.respond(to: firstRequest, withResult: .invalidAttributeValueLength); return }
            
            print("receive message from ", request.central, String(bytes: data, encoding: .utf8)!)
        }
        peripheral.respond(to: firstRequest, withResult: .success)
    }
    
    // MARK: Peripheral client
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if error != nil {
            print("service discovery failed: ", error!)
        } else {
            guard let firstService = peripheral.services?.first else { return }
            
            peripheral.discoverCharacteristics([BluetoothRadio.chracteristicUUID], for: firstService)
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        if error != nil {
            print("characteristic discovery failed: ", error!)
        } else {
            guard let firstCharacteristic = service.characteristics?.first else { return }
            
            guard let data = "HOLA MUNDO xd".data(using: .utf8) else { return }
            
            peripheral.writeValue(data, for: firstCharacteristic, type: .withoutResponse)
        }
    }
}

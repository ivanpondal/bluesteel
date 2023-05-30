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

struct BluetoothError : Error {
    var message: String
}

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

    var serviceDiscoveryContinuation: CheckedContinuation<CBPeripheral, Error>? = nil
    var characteristicDiscoveryContinuation: CheckedContinuation<CBPeripheral, Error>? = nil
    var writeWithResponseContinuation: CheckedContinuation<Bool, Error>? = nil

    func start() {
        centralManager = CBCentralManager(delegate: self, queue: .global(qos: .utility), options: [CBCentralManagerOptionShowPowerAlertKey: true])
        peripheralManager = CBPeripheralManager(delegate: self, queue: .global(qos: .utility), options: [CBPeripheralManagerOptionShowPowerAlertKey: true])
    }
    
    private func peripheral(withId peripheralUUID: UUID) throws -> CBPeripheral {
        guard let peripheral = centralManager?.retrievePeripherals(withIdentifiers: [peripheralUUID])[0] else {
            throw BluetoothError(message: "Could not find peripheral")
        }
        return peripheral
    }
    
    func connect(toPeripheralWithId uuid: UUID) {
        if (!connectedPeripherals.contains(where: {$0.key == uuid})){
            do {
                let peripheral = try peripheral(withId: uuid)

                connectedPeripherals.updateValue(peripheral, forKey: uuid)

                centralManager?.connect(peripheral)
            } catch {
                print(error)
            }
        }
    }

    func discover(fromPeripheralWithId peripheralId: UUID, serviceId: CBUUID) async throws -> CBPeripheral {
        let peripheral = try peripheral(withId: peripheralId)

        return try await withCheckedThrowingContinuation({continuation in
            peripheral.discoverServices([serviceId])
            serviceDiscoveryContinuation = continuation
        })
    }

    func discover(fromPeripheralWithId peripheralId: UUID, serviceId: CBUUID, characteristicId: CBUUID) async throws -> CBPeripheral {
        let peripheral = try peripheral(withId: peripheralId)

        return try await withCheckedThrowingContinuation({continuation in
            guard let service = peripheral.services?.first(where: {$0.uuid == serviceId}) else {
                continuation.resume(throwing: BluetoothError(message: "Service not found"))
                return
            }

            peripheral.discoverCharacteristics([characteristicId], for: service)
            characteristicDiscoveryContinuation = continuation
        })
    }

    func writeWithResponse(toPeripheralWithId peripheralId: UUID, serviceId: CBUUID, characteristicId: CBUUID, data: Data) async throws {
        let peripheral = try peripheral(withId: peripheralId)
        guard let characteristic = peripheral.services?.first(where: {$0.uuid == serviceId})?
            .characteristics?.first(where: {$0.uuid == characteristicId}) else {
            throw BluetoothError(message: "Could not find characteristic")
        }

        let _ = try await withCheckedThrowingContinuation({continuation in
            peripheral.writeValue(data, for: characteristic, type: .withResponse)
            writeWithResponseContinuation = continuation
        })
    }

    func mtu(forPeripheralId peripheralId: UUID, withWriteType type: CBCharacteristicWriteType) throws -> Int {
        let peripheral = try peripheral(withId: peripheralId)

        return peripheral.maximumWriteValueLength(for: type)
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
        let characteristic = CBMutableCharacteristic(type: BluetoothRadio.chracteristicUUID, properties: [.writeWithoutResponse, .write], value: nil, permissions: [.writeable])
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
            
            print("receive message from ", request.central, String(bytes: data, encoding: .ascii)!)
        }
        peripheral.respond(to: firstRequest, withResult: .success)
    }

    // MARK: Peripheral client

    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if let error {
            print("service discovery failed: ", error)
            guard let continuation = serviceDiscoveryContinuation else {
                print("missing service discovery continuation")
                return
            }
            continuation.resume(throwing: error)
            return
        }
        guard let continuation = serviceDiscoveryContinuation else {
            print("missing service discovery continuation")
            return
        }
        continuation.resume(returning: peripheral)
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        if let error {
            print("characteristic discovery failed: ", error)
            guard let continuation = characteristicDiscoveryContinuation else {
                print("missing characteristic discovery continuation")
                return
            }
            continuation.resume(throwing: error)
            return
        }

        guard let continuation = characteristicDiscoveryContinuation else {
            print("missing characteristic discovery continuation")
            return
        }

        continuation.resume(returning: peripheral)
    }


    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        if let error {
            print("write failed: ", error)
            guard let continuation = writeWithResponseContinuation else {
                return
            }
            continuation.resume(throwing: error)
            return
        }

        guard let continuation = writeWithResponseContinuation else {
            print("missing write with response continuation")
            return
        }

        continuation.resume(returning: true)
    }

    func peripheral(_ peripheral: CBPeripheral, didModifyServices invalidatedServices: [CBService]) {
        print("update service \(peripheral)")
    }
}

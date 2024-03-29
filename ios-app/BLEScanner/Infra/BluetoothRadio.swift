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
    static let shared = BluetoothRadio()

    var centralManager: CBCentralManager?
    var peripheralManager: CBPeripheralManager?

    var stateSubject: CurrentValueSubject<CBManagerState, Never> = .init(.unknown)
    var peripheralSubject: PassthroughSubject<PeripheralAdvertisement, Never> = .init()

    private var connectedPeripherals: Dictionary<UUID, CBPeripheral> = [:]

    var connectionEventSubject: PassthroughSubject<CBPeripheral, Never> = .init()
    var disconnectionEventSubject: PassthroughSubject<CBPeripheral, Never> = .init()

    var peripheralDiscoveryContinuation: CheckedContinuation<CBPeripheral, Never>? = nil
    var peripheralConnectionContinuation: CheckedContinuation<CBPeripheral, Never>? = nil

    var serviceRegistrationContinuation: CheckedContinuation<CBService, Error>? = nil
    var serviceAdvertisementContinuation: CheckedContinuation<Bool, Error>? = nil

    var serviceDiscoveryContinuation: CheckedContinuation<CBPeripheral, Error>? = nil
    var characteristicDiscoveryContinuation: CheckedContinuation<CBPeripheral, Error>? = nil
    var writeWithResponseContinuation: CheckedContinuation<Bool, Error>? = nil

    private var peripheralManagerReady: Bool = false

    private var peripheralReadyHandler: () -> Void = {}

    private var writeHandlers: [CBUUID: (_ centra: CBCentral, _ data: Data) -> Void] = [:]

    func start(onPeripheralReady peripheralReadyHandler: @escaping ()-> Void = {}) {
        centralManager = CBCentralManager(delegate: self, queue: .global(qos: .utility), options: [CBCentralManagerOptionShowPowerAlertKey: true])
        peripheralManager = CBPeripheralManager(delegate: self, queue: .global(qos: .utility), options: [CBPeripheralManagerOptionShowPowerAlertKey: true])
        self.peripheralReadyHandler = peripheralReadyHandler
    }

    func stopScan() {
        centralManager?.stopScan()
    }

    func disconnect(fromPeripheralWithId peripheralId: UUID) throws {
        let peripheral = try peripheral(withId: peripheralId)

        centralManager?.cancelPeripheralConnection(peripheral)
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

    func discover(peripheralWithService serviceId: CBUUID) async -> CBPeripheral {
        return await withCheckedContinuation({continuation in
            centralManager?.scanForPeripherals(withServices: [serviceId])
            peripheralDiscoveryContinuation = continuation
        })
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

    func connect(toPeripheralWithId peripheralId: UUID) async throws -> CBPeripheral {
        let peripheral = try peripheral(withId: peripheralId)

        return await withCheckedContinuation({continuation in
            centralManager?.connect(peripheral)
            peripheralConnectionContinuation = continuation
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

    func publish(service: PeripheralService, withLocalName localName: String) async throws {
        if let peripheralManager {
            let _ = try await withCheckedThrowingContinuation({continuation in
                peripheralManager.removeAllServices()
                peripheralManager.add(service.toMutableService())
                serviceRegistrationContinuation = continuation
            })
            peripheralManager.stopAdvertising()
            let _ = try await withCheckedThrowingContinuation({continuation in
                peripheralManager.startAdvertising([CBAdvertisementDataLocalNameKey: localName, CBAdvertisementDataServiceUUIDsKey: [service.serviceId]])
                serviceAdvertisementContinuation=continuation
            })
            writeHandlers.updateValue(service.writeHandler, forKey: service.characteristicId)
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
            centralManager?.scanForPeripherals(withServices: [BluetoothRadio.serviceUUID, TestCase.writeTestServiceUUID], options: [CBCentralManagerScanOptionAllowDuplicatesKey: true])
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
            guard let continuation = peripheralDiscoveryContinuation else {
                return
            }
            continuation.resume(returning: peripheral)
            peripheralDiscoveryContinuation = nil
        }
    }

    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        peripheral.delegate = self

        connectionEventSubject.send(peripheral)
        print("connected to ", peripheral)

        guard let continuation = peripheralConnectionContinuation else {
            return
        }
        continuation.resume(returning: peripheral)
        peripheralConnectionContinuation = nil
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

    static func createService() -> PeripheralService {
        return PeripheralService(serviceId: BluetoothRadio.serviceUUID, characteristicId: BluetoothRadio.chracteristicUUID, writeHandler: {central, data in
                print("receive message from ", central, String(bytes: data, encoding: .ascii)!)
        })
    }

    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        switch peripheral.state {
        case .poweredOn:
            self.peripheralManagerReady = true
            peripheralReadyHandler()
            peripheralReadyHandler = {}
        case .poweredOff:
            self.peripheralManagerReady = false
            print("peripheral is down")
        default:
            self.peripheralManagerReady = false
            print("other")
        }
    }

    func peripheralManager(_ peripheral: CBPeripheralManager, didAdd service: CBService, error: Error?) {
        if let error {
            print("something went wrong adding peripheral service: ", error.localizedDescription)

            guard let continuation = serviceRegistrationContinuation else {
                print("missing service registration continuation")
                return
            }
            continuation.resume(throwing: error)
            return
        } else {
            print("peripheral service is up")
            guard let continuation = serviceRegistrationContinuation else {
                print("missing service registration continuation")
                return
            }
            continuation.resume(returning: service)
        }
    }

    func peripheralManagerDidStartAdvertising(_ peripheral: CBPeripheralManager, error: Error?) {
        if let error {
            print("something went wrong advertising peripheral service: ", error.localizedDescription)
            guard let continuation = serviceAdvertisementContinuation else {
                print("missing service advertisement continuation")
                return
            }
            continuation.resume(throwing: error)
        } else {
            print("peripheral is advertising service")
            guard let continuation = serviceAdvertisementContinuation else {
                print("missing service advertisement continuation")
                return
            }
            continuation.resume(returning: true)
        }
    }

    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveWrite requests: [CBATTRequest]) {
        let firstRequest = requests[0]

        for request in requests {
            guard let data = request.value else { peripheral.respond(to: firstRequest, withResult: .invalidAttributeValueLength); return }

            if let handler = writeHandlers[request.characteristic.uuid] {
                handler(request.central, data)
            } else {
                print("could not find handler for message from ", request.central, String(bytes: data, encoding: .ascii)!)
            }
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

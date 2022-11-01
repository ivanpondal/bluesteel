//
//  ContentView.swift
//  BLEScanner
//
//  Created by iván pondal on 10/10/2022.
//
import SwiftUI
import CoreBluetooth
import Combine

struct DeviceListView: View {
    
    var bluetoothRadio: BluetoothRadio = BluetoothRadio()
    
    @State var bluetoothState: String = "Loading"
    @State var devices: [Device] = []
    
    var body: some View {
        NavigationView {
            VStack {
                Text("Bluetooth state: \(bluetoothState)")
                List(devices) { device in
                    NavigationLink {
                        DeviceAdvDataView(device: device, onDeviceConnect: {
                            bluetoothRadio.connect(toPeripheralWithId: $0)
                        })
                    } label: {
                        DeviceView(device: device)
                    }
                }
            }
            .onAppear {
                bluetoothRadio.start()
            }
            .navigationBarHidden(true)
        }
        .onReceive(bluetoothRadio.stateSubject.receive(on: RunLoop.main)) {
            switch $0 {
                case .unsupported:
                    bluetoothState = "Unsupported"
                case .poweredOn:
                    bluetoothState = "Powered on"
                case .poweredOff:
                    bluetoothState = "Powered off"
                case .unauthorized:
                    bluetoothState = "Unauthorized"
                case .resetting:
                    bluetoothState = "Resetting"
                default:
                    bluetoothState = "Unknown"
            }
        }
        .onReceive(bluetoothRadio.peripheralSubject
            .receive(on: RunLoop.main)
            .throttle(for: .seconds(1), scheduler: RunLoop.main, latest: true)
            .map({ peripheralWithSignal -> Device in
                var advertisedServices: [UUID] = []
                
                let uuidAdvertisements = peripheralWithSignal.advertisements[CBAdvertisementDataServiceUUIDsKey] as? [CBUUID] ?? []
                
                for uuidAdvertisement in uuidAdvertisements {
                    if let uuid = UUID(uuidString: uuidAdvertisement.uuidString) {
                        advertisedServices.append(uuid)
                    }
                }
                
                return Device(id: peripheralWithSignal.peripheral.identifier,
                              rssi: peripheralWithSignal.rssi,
                              name: peripheralWithSignal.peripheral.name,
                              advertisedServices: advertisedServices)
            })) { device in
            if let deviceIndex = devices.firstIndex(where: { oldDevice in oldDevice.id == device.id }) {
                devices[deviceIndex] = device
            } else {
                devices.append(device)
            }
            devices.sort { deviceLeft, deviceRight in
                deviceLeft.rssi > deviceRight.rssi
            }
        }
    }
}

struct DeviceListView_Previews: PreviewProvider {
    static var previews: some View {
        DeviceListView(devices: Device.sampleData)
    }
}

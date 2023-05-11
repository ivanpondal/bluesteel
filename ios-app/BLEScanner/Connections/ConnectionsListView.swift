//
//  ConnectionsListView.swift
//  BLEScanner
//
//  Created by iván pondal on 27/04/2023.
//

import Foundation
import SwiftUI

struct ConnectionsListView: View {
    var bluetoothRadio: BluetoothRadio
    @State var connectedDevices: [Device] = []
    @State private var selectedDevices: [UUID: Bool]
    @State private var testCase: String = ""
    private let testCases = ["SR-OW-1"]

    init(bluetoothRadio: BluetoothRadio, connectedDevices: [Device] = []) {
        self.bluetoothRadio = bluetoothRadio
        self.connectedDevices = connectedDevices
        self.selectedDevices = connectedDevices.reduce(into: [UUID: Bool]()) {
            $0[$1.id] = false
        }
    }

    var body: some View {
        NavigationStack {
            VStack {
                List(connectedDevices) { connectedDevice in
                    Toggle(connectedDevice.id.uuidString,
                           isOn: .init(
                            get: { selectedDevices[connectedDevice.id, default: false] },
                            set: { selectedDevices[connectedDevice.id] = $0 }))
                    .toggleStyle(.switch)
                }
                Spacer()
                HStack{
                    Text("Test case")
                    Divider()
                    Picker("", selection: $testCase){
                        ForEach(testCases, id: \.self) {
                            Text($0)
                        }
                    }.pickerStyle(.menu)
                }.fixedSize()
                NavigationLink(destination: TestCaseView(activeTestCase: TestCase.sampleData.first!).navigationBarHidden(true)) {
                    Button("Run", action: {}).allowsHitTesting(false)
                }
            }
        }
        .onReceive(bluetoothRadio.connectionEventSubject
            .receive(on: RunLoop.main)
            .map({ peripheral in
                return Device(id: peripheral.identifier,rssi: 0, name:peripheral.name, advertisedServices: [])
            })){ connectedDevices.append($0) }
        .onReceive(bluetoothRadio.disconnectionEventSubject.receive(on: RunLoop.main)){ device in
                connectedDevices.removeAll(where: {$0.id == device.identifier})
            }
    }
}

struct ConnectionsListView_Previews: PreviewProvider {
    static var previews: some View {
        ConnectionsListView(bluetoothRadio: BluetoothRadio(), connectedDevices: Device.sampleData)
    }
}

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
    @State private var toggle: Bool = false
    @State private var testCase: String = ""
    private let testCases = ["SR-OW-1"]
    
    var body: some View {
        VStack{
            List(connectedDevices) { connectedDevice in
                Toggle("\(connectedDevice.id)", isOn: $toggle).toggleStyle(.switch)
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
            Button("Run", action: {
            }).padding()
        }.onReceive(bluetoothRadio.connectionEventSubject
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

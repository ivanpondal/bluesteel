//
//  ConnectionsListView.swift
//  BLEScanner
//
//  Created by iván pondal on 27/04/2023.
//

import Foundation
import SwiftUI

struct TestCaseListView: View {
    var bluetoothRadio: BluetoothRadio
    @State private var connectedDevices: [Device] = []
    @State private var selectedDevices: [UUID: Bool] = [:]
    @State private var selectedTestCase: TestCaseId = TestCaseId.SR_OW_1

    @State private var selectedRole: TestCaseRole = .SENDER

    func NavigationViewWrapper(@ViewBuilder content: () -> some View) -> some View {
        if #available(iOS 16, *) {
            return NavigationStack(root: content)
        } else {
            return NavigationView(content: content)
        }
    }

    var body: some View {
        NavigationViewWrapper {
            VStack {
                switch selectedTestCase {
                case .SR_OW_1:
                    List(connectedDevices) { connectedDevice in
                        Toggle(connectedDevice.id.uuidString,
                               isOn: .init(
                                get: { selectedDevices[connectedDevice.id, default: false] },
                                set: { selectedDevices[connectedDevice.id] = $0 }))
                        .toggleStyle(.switch)
                    }
                case .SR_OW_2:
                    HStack(){
                        Text("Role")
                        Divider()
                        Picker("", selection: $selectedRole){
                            Text("Sender").tag(TestCaseRole.SENDER)
                            Text("Receiver").tag(TestCaseRole.RECEIVER)
                        }.pickerStyle(.menu)
                    }.fixedSize().padding(.top, 24)
                case .SR_OW_3:
                    HStack(){
                        Text("Role")
                        Divider()
                        Picker("", selection: $selectedRole){
                            Text("Sender").tag(TestCaseRole.SENDER)
                            Text("Receiver").tag(TestCaseRole.RECEIVER)
                        }.pickerStyle(.menu)
                    }.fixedSize().padding(.top, 24)
                }
                Spacer()
                HStack{
                    Text("Test case")
                    Divider()
                    Picker("", selection: $selectedTestCase){
                        ForEach(TestCaseId.allCases, id: \.self) {
                            Text($0.displayName()).tag($0)
                        }
                    }.pickerStyle(.menu)
                }.fixedSize()
                NavigationLink(destination: TestCaseView(activeTestCase: TestCase(
                    id: selectedTestCase,
                    role: selectedRole), bluetoothRadio: bluetoothRadio, targetDevice: connectedDevices.filter({selectedDevices[$0.id] == true}).first
                )) {
                    Button("Run", action: {}).allowsHitTesting(false)
                }
            }
        }
        .onAppear {
            bluetoothRadio.stopScan()
        }
        // Use connection/disconnection events to make it easier to keep track of selected devices
        .onReceive(bluetoothRadio.connectionEventSubject
            .receive(on: RunLoop.main)
            .map({ peripheral in
                return Device(id: peripheral.identifier,rssi: 0, name:peripheral.name, advertisedServices: [])
            })){
                connectedDevices.append($0)
                selectedDevices[$0.id] = false
            }
            .onReceive(bluetoothRadio.disconnectionEventSubject
                .receive(on: RunLoop.main)){ device in
                    connectedDevices.removeAll(where: {$0.id == device.identifier})
                    selectedDevices.removeValue(forKey: device.identifier)
                }
    }
}

struct TestCaseListView_Previews: PreviewProvider {
    static var previews: some View {
        TestCaseListView(bluetoothRadio: BluetoothRadio())
    }
}

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

    @State private var selectedRole: TestCaseRole = .A
    @State private var selectedNodeIndex: Int = 0

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
                    DropdownPicker(selection: $selectedRole, pickerLabel: "Role",
                                   options: [(TestCaseRole.A, "Sender"), (TestCaseRole.B, "Receiver")])
                    .padding(.top, 24)
                case .SR_OW_3:
                    DropdownPicker(selection: $selectedRole, pickerLabel: "Role",
                                   options: [(TestCaseRole.A, "Sender"), (TestCaseRole.B, "Receiver")])
                    .padding(.top, 24)
                case .SR_OW_4:
                    DropdownPicker(selection: $selectedRole, pickerLabel: "Role",
                                   options: [(TestCaseRole.A, "Foreground"), (TestCaseRole.B, "Background")])
                    .padding(.top, 24)
                case .SR_OW_5:
                    DropdownPicker(selection: $selectedRole, pickerLabel: "Role",
                                   options: [(TestCaseRole.A, "Sender"), (TestCaseRole.B, "Relay"), (TestCaseRole.C, "Receiver")])
                    .padding(.top, 24)
                    if (selectedRole != TestCaseRole.A) {
                        DropdownPicker(selection: $selectedNodeIndex, pickerLabel: "Node Index",
                                       options: Array(0...4).map({ ($0, "\($0)") }))
                        .padding(.top, 24)
                    }
                }
                Spacer()
                DropdownPicker(selection: $selectedTestCase, pickerLabel: "Test case",
                               options: TestCaseId.allCases.map { ($0, $0.displayName())})
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

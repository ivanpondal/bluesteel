//
//  ConnectionsListView.swift
//  BLEScanner
//
//  Created by iván pondal on 27/04/2023.
//

import Foundation
import SwiftUI

struct ConnectionsListView: View {
    @State var connectedDevices: [Device] = []
    @State private var toggle: Bool = false
    @State private var testCase: String = ""
    private let testCases = ["SR-OW-1"]

    var body: some View {
        VStack{
            List(connectedDevices) { connectedDevice in
                Toggle("\(connectedDevice.id)", isOn: $toggle).toggleStyle(.switch)
            }
            Picker("Select test case", selection: $testCase){
                ForEach(testCases, id: \.self) {
                    Text($0)
                }
            }
            Button("Run test", action: {
            }).padding()
        }
    }
}

struct ConnectionsListView_Previews: PreviewProvider {
    static var previews: some View {
        ConnectionsListView(connectedDevices: Device.sampleData)
    }
}

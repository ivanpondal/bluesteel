//
//  TestCaseView.swift
//  BLEScanner
//
//  Created by iván pondal on 04/05/2023.
//

import Foundation
import SwiftUI

struct TestCaseView: View {

    var activeTestCase: TestCase
    var bluetoothRadio: BluetoothRadio

    var body: some View {
        VStack{
            Text("Test case: \"\(activeTestCase.id.displayName())\"")
                .font(.title)
                .padding()
                .frame(maxWidth: .infinity)
            List(activeTestCase.devices) { connectedDevice in
                VStack {
                    Text(connectedDevice.id.uuidString)
                        .font(.subheadline).padding(4)
                    Text("Current state: RUNNING 🏃‍♂️")
                }.padding().frame(maxWidth: .infinity)
            }.listStyle(.grouped)

            Spacer()
            Button("Stop", action: {})
        }.task {
            let testRunner = TestRunner(bluetoothRadio: bluetoothRadio, testCase: activeTestCase, device: activeTestCase.devices.first!)

            await testRunner.run()
        }
    }
}

struct TestCaseView_Previews: PreviewProvider {

    static var previews: some View {
        TestCaseView(activeTestCase: TestCase.sampleData.first!, bluetoothRadio: BluetoothRadio())
    }
}

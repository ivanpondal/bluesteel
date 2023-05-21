//
//  TestCaseView.swift
//  BLEScanner
//
//  Created by iván pondal on 04/05/2023.
//

import Foundation
import SwiftUI
import Combine

struct TestCaseView: View {

    var activeTestCase: TestCase
    var bluetoothRadio: BluetoothRadio
    @State
    private var testRunnerState: String = "N/A"
    @State
    private var cancellables: Set<AnyCancellable> = Set<AnyCancellable>()

    private func runTest() async {
        if (activeTestCase.devices.count > 0){
            let testRunner = TestRunner(bluetoothRadio: bluetoothRadio, testCase: activeTestCase, device: activeTestCase.devices.first!)
            testRunner.$state.sink(receiveValue: { testRunnerState = $0 }).store(in: &cancellables)
            await testRunner.run()
        }
    }

    var body: some View {
        VStack {
            Text("Test case: \"\(activeTestCase.id.displayName())\"")
                .font(.title)
                .padding()
                .frame(maxWidth: .infinity)
            List(activeTestCase.devices) { connectedDevice in
                VStack {
                    Text(connectedDevice.id.uuidString)
                        .font(.subheadline).padding(4)
                    Text("Current state: \(testRunnerState)")
                }.padding().frame(maxWidth: .infinity)
            }.listStyle(.grouped)

            Spacer()
            Button("Restart", action: {
                Task {
                    await runTest()
                }
            }).disabled(testRunnerState == "RUNNING 🏃‍♂️")
        }.navigationBarBackButtonHidden(testRunnerState != "FINISHED ☑️").task {
            await runTest()
        }
    }
}

struct TestCaseView_Previews: PreviewProvider {

    static var previews: some View {
        TestCaseView(activeTestCase: TestCase.sampleData.first!, bluetoothRadio: BluetoothRadio())
    }
}

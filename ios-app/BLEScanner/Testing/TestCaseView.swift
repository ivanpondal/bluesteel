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

    private static let COPY_RESULTS_TEXT = "Copy results"

    var activeTestCase: TestCase
    var bluetoothRadio: BluetoothRadio

    @State
    private var testRunnerState: String = "N/A"
    @State
    private var testRunnerPacketsSent: Int = 0
    @State
    private var testRunnerBytesSentPerSec: Float = 0
    @State
    private var testRunnerMtu: Int = 0
    @State
    private var cancellables: Set<AnyCancellable> = Set<AnyCancellable>()
    @State
    private var copyButtonText = COPY_RESULTS_TEXT
    @State
    private var testRunOutput: String = ""

    private func runTest() async {
        guard let testCaseDevice = activeTestCase.device else { return }
        copyButtonText = TestCaseView.COPY_RESULTS_TEXT
        let testRunner = TestRunner(bluetoothRadio: bluetoothRadio, testCase: activeTestCase, device: testCaseDevice)
        testRunner.$state.sink(receiveValue: { testRunnerState = $0 }).store(in: &cancellables)
        testRunner.$packetsSent
            .throttle(for: .seconds(0.5), scheduler: RunLoop.main, latest: true)
            .sink(receiveValue: { testRunnerPacketsSent = $0 })
            .store(in: &cancellables)
        testRunner.$bytesSentPerSecond
            .throttle(for: .seconds(0.5), scheduler: RunLoop.main, latest: true)
            .sink(receiveValue: { testRunnerBytesSentPerSec = $0 })
            .store(in: &cancellables)
        testRunner.$mtu
            .sink(receiveValue: {testRunnerMtu = $0})
            .store(in: &cancellables)
        await testRunner.run()
        testRunOutput = testRunner.consoleOutput
    }

    var body: some View {
        VStack {
            Text("Test case: \"\(activeTestCase.id.displayName())\"")
                .font(.title)
                .padding()
                .frame(maxWidth: .infinity)
            List(activeTestCase.device != nil ? [activeTestCase.device!] : []) { connectedDevice in
                TestCaseRunView(testDevice: connectedDevice,
                                testRunnerState: testRunnerState,
                                packetsSent: testRunnerPacketsSent,
                                bytesPerSecond: testRunnerBytesSentPerSec,
                                mtu: testRunnerMtu)
            }.listStyle(.grouped)

            Spacer()
            Button("\(copyButtonText)", action: {
                let pasteboard = UIPasteboard.general
                pasteboard.string = testRunOutput
                copyButtonText = "Copied!"
            }).disabled(testRunnerState == "RUNNING 🏃‍♂️").padding()
            Button("Restart", action: {
                Task {
                    await runTest()
                }
            }).disabled(testRunnerState == "RUNNING 🏃‍♂️").padding()
        }.navigationBarBackButtonHidden(testRunnerState != "FINISHED ☑️").task { await runTest() }
    }
}

struct TestCaseView_Previews: PreviewProvider {

    static var previews: some View {
        TestCaseView(activeTestCase: TestCase.sampleData.first!, bluetoothRadio: BluetoothRadio())
    }
}

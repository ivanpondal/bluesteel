//
//  BLEScannerApp.swift
//  BLEScanner
//
//  Created by iván pondal on 10/10/2022.
//

import SwiftUI
import BackgroundTasks

@main
struct BLEScannerApp: App {

    @Environment(\.scenePhase) var scenePhase

    var bluetoothRadio: BluetoothRadio = BluetoothRadio.shared

    init() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "com.blescanner.srow3", using: nil) {
            task in
            let testTask = Task {
                let testRunner = TestRunner(bluetoothRadio: BluetoothRadio.shared, testCase: TestCase(id: .SR_OW_2, role: .A, nodeIndex: 0), targetDevice: nil)
                await testRunner.run()
                task.setTaskCompleted(success: true)
            }

            task.expirationHandler = {
                print("SR-OW-3 Background task was cancelled")
                testTask.cancel()
                task.setTaskCompleted(success: false)
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            TabView {
                DeviceListView(bluetoothRadio: bluetoothRadio)
                    .tabItem {
                        Label("Scanner", systemImage: "magnifyingglass")
                    }
                TestCaseListView(bluetoothRadio: bluetoothRadio)
                    .tabItem{
                        Label("Connections", systemImage: "link")
                    }
            }
        }
        .onChange(of: scenePhase) { newPhase in
            if newPhase == .active {
                print("Active")
            } else if newPhase == .inactive {
                print("Inactive")
            } else if newPhase == .background {
                print("Background")
            }
        }
    }
}

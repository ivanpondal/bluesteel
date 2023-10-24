//
//  BLEScannerApp.swift
//  BLEScanner
//
//  Created by iván pondal on 10/10/2022.
//

import SwiftUI

@main
struct BLEScannerApp: App {

    @Environment(\.scenePhase) var scenePhase

    var bluetoothRadio: BluetoothRadio = BluetoothRadio()

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

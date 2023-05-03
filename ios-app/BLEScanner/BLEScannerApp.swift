//
//  BLEScannerApp.swift
//  BLEScanner
//
//  Created by iván pondal on 10/10/2022.
//

import SwiftUI

@main
struct BLEScannerApp: App {

    var bluetoothRadio: BluetoothRadio = BluetoothRadio()

    var body: some Scene {
        WindowGroup {
            TabView {
                DeviceListView(bluetoothRadio: bluetoothRadio)
                    .tabItem {
                        Label("Scanner", systemImage: "magnifyingglass")
                    }
                ConnectionsListView(bluetoothRadio: bluetoothRadio)
                    .tabItem{
                        Label("Connections", systemImage: "link")
                    }
            }
        }
    }
}

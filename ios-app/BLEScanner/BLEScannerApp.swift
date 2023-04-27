//
//  BLEScannerApp.swift
//  BLEScanner
//
//  Created by iván pondal on 10/10/2022.
//

import SwiftUI

@main
struct BLEScannerApp: App {
    var body: some Scene {
        WindowGroup {
            TabView {
                DeviceListView()
                    .tabItem {
                        Label("Scanner", systemImage: "magnifyingglass")
                    }
                ConnectionsListView()
                    .tabItem{
                        Label("Connections", systemImage: "link")
                    }
            }
        }
    }
}

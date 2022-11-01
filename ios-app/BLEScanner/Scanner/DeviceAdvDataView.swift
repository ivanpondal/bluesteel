//
//  DeviceAdvDataView.swift
//  BLEScanner
//
//  Created by iván pondal on 28/10/2022.
//

import Foundation

import SwiftUI

struct DeviceAdvDataView: View {
    
    let device: Device
    let onDeviceConnect: (UUID) -> Void
    
    var body: some View {
        VStack {
            Text(device.id.uuidString)
                .font(.title)
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
            Text("Name: \(device.name ?? "<no name>")")
                .font(.body)
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
            Text("RSSI: \(device.rssi)")
                .font(.body)
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
            Text("Advertisement data")
                .font(.title2)
                .fontWeight(.bold)
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
            List(device.advertisedServices, id: \.hashValue) { serviceUUID in
                Text("\(serviceUUID.uuidString)")
            }
            Spacer()
            Button("Connect", action: {
                print("connecting")
                
                onDeviceConnect(device.id)
            })
                .padding()
        }
    }
}

struct DeviceAdvDataView_Previews: PreviewProvider {
    static var previews: some View {
        DeviceAdvDataView(device: Device.sampleData[0], onDeviceConnect: { _ in })
    }
}

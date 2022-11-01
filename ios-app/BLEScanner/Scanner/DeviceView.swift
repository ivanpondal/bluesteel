//
//  DeviceRow.swift
//  BLEScanner
//
//  Created by iván pondal on 24/10/2022.
//

import Foundation
import SwiftUI

struct DeviceView: View {
    let device: Device
    
    var body: some View {
        VStack(alignment: .leading){
            Text(device.id.uuidString)
                .font(.caption)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
            Spacer()
            Text(device.name ?? "<no name>")
            HStack {
                Text("Advertised services:").font(.footnote)
                Circle()
                    .frame(width: 16, height: 16)
                    .foregroundColor(device.advertisedServices.count == 0 ? .gray : .black)
                    .overlay {
                        Text("\(device.advertisedServices.count)")
                            .font(.caption)
                            .foregroundColor(.white)
                    }
                Spacer()
                Text("RSSI: \(device.rssi)").font(.footnote)
            }
        }
        .frame(maxWidth: .infinity)
        .padding()
    }
}

struct DeviceView_Previews: PreviewProvider {
    static var previews: some View {
        DeviceView(device: Device.sampleData[0])
            .previewLayout(.fixed(width: 400, height: 90))
            .previewDisplayName("Device with name")
        
        DeviceView(device: Device.sampleData[2])
            .previewLayout(.fixed(width: 400, height: 90))
            .previewDisplayName("Device without name")
    }
}

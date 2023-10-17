//
//  TestCaseRunView.swift
//  BLEScanner
//
//  Created by iván pondal on 25/05/2023.
//

import Foundation
import SwiftUI

struct TestCaseRunView: View {
    let testDevice: Device
    let testRunnerState: String
    let packetsSent: Int
    let bytesPerSecond: Float
    let mtu: Int

    var body: some View {
        VStack {
            Text("\(testRunnerState)").bold()
            Text(testDevice.id.uuidString)
                .font(.subheadline).padding(4)
            VStack(alignment: .leading) {
                Text("💾 mtu: \(mtu) bytes").font(.callout).padding(.bottom, 2)
                Text("⬆️ uplink: \(round(bytesPerSecond/1024*100)/100, specifier: "%.2f") kbytes/s").font(.callout).padding(.bottom, 2)
                Text("#️⃣ packets sent: \(packetsSent)").font(.callout)
            }.padding(.top, 8).frame(maxWidth: .infinity, alignment: .leading)
        }.padding()
    }
}

struct TestCaseRunView_Previews: PreviewProvider {

    static var previews: some View {
        TestCaseRunView(testDevice: Device.sampleData.first!, testRunnerState: "RUNNING 🏃‍♂️", packetsSent: 23, bytesPerSecond: 1033.34344, mtu: 512)
            .previewLayout(.fixed(width: 400, height: 180))
            .previewDisplayName("Test running")
    }
}

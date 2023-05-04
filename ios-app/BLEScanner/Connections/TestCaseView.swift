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

    var body: some View {
        VStack{
            Text("Test case: \"\(activeTestCase.id)\"")
                .font(.title)
                .padding()
                .frame(maxWidth: .infinity)
            Text("Current state: RUNNING")
            Spacer()
            Button("Stop", action: {})
        }
    }
}

struct TestCaseView_Previews: PreviewProvider {

    static var previews: some View {
        TestCaseView(activeTestCase: TestCase.sampleData.first!)
    }
}

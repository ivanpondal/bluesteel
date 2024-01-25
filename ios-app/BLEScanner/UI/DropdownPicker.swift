//
//  DropdownPicker.swift
//  BLEScanner
//
//  Created by iván pondal on 18/01/2024.
//

import Foundation
import SwiftUI

struct DropdownPicker<T: Hashable>: View {
    @Binding var selection: T

    let pickerLabel: String
    let options: Array<(T, String)>

    var body: some View {
        HStack{
            Text(pickerLabel)
            Divider()
            Picker("", selection: $selection){
                ForEach(options, id: \.0) { (key, value) in
                    Text(value).tag(key)
                }
            }.pickerStyle(.menu)
        }.fixedSize()
    }
}

struct DropdownPicker_Previews: PreviewProvider {

    static var previews: some View {
        DropdownPicker(selection: .constant(TestCaseId.SR_OW_1),
                       pickerLabel: "Test case",
                       options: TestCaseId.allCases.map { testCaseId in (testCaseId, testCaseId.displayName())}
                       )
        .previewLayout(.fixed(width: 400, height: 90))
        .previewDisplayName("Dropdown")
    }
}

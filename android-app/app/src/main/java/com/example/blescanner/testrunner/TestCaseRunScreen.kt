package com.example.blescanner.testrunner

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.example.blescanner.testrunner.model.TestCaseId

@Composable
fun TestCaseRun(
    testCase: TestCaseId,
    selectedDevices: Set<String>,
) {
    Column {
       Text("Test case: ${testCase.displayName}")
    }
}

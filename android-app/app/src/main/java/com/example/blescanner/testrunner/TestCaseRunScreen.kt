package com.example.blescanner.testrunner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blescanner.model.BluetoothDeviceData
import com.example.blescanner.testrunner.model.TestCaseId
import com.example.blescanner.ui.theme.BLEScannerTheme

@Composable
fun TestCaseRun(
    testCase: TestCaseId,
    selectedDevices: Set<String>,
    testRunnerState: String
) {
    Column(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Test case: \"${testCase.displayName}\"", fontSize = 24.sp)

        TestCaseRunView(testRunnerState = testRunnerState)
    }
}

@Preview
@Composable
fun TestCaseRunPreview() {
    BLEScannerTheme {
        TestCaseRun(
            testCase = TestCaseId.SR_OW_1,
            selectedDevices = setOf(BluetoothDeviceData.sampleDevices.first().id),
            testRunnerState = ""
        )
    }
}

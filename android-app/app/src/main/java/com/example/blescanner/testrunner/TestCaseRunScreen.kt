package com.example.blescanner.testrunner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blescanner.model.BluetoothDeviceData
import com.example.blescanner.testrunner.model.TestCaseId
import com.example.blescanner.testrunner.services.TestRunner.Companion.RUNNING_EMOJI
import com.example.blescanner.ui.theme.BLEScannerTheme

val COPY_RESULTS_TEXT = "Copy results"

@Composable
fun TestCaseRun(
    testCase: TestCaseId,
    selectedDevices: Set<String>,
    testRunnerState: String,
    testRunnerPacketsSent: Int,
    testRunnerBytesPerSecond: Float,
    onCopyResults: () -> Unit,
    onRestart: () -> Unit
) {
    var copyResultsText by remember {
        mutableStateOf(COPY_RESULTS_TEXT)
    }
    Column(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Test case: \"${testCase.displayName}\"",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TestCaseRunRow(
            testRunnerState = testRunnerState,
            selectedDevices.first(),
            testRunnerPacketsSent,
            testRunnerBytesPerSecond
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                onCopyResults()
                copyResultsText = "Copied!"
            },
            enabled = testRunnerState != "RUNNING $RUNNING_EMOJI"
        ) {
            Text(copyResultsText, fontSize = 16.sp)
        }
        Button(onClick = {
            copyResultsText = COPY_RESULTS_TEXT
            onRestart()
        }, enabled = testRunnerState != "RUNNING $RUNNING_EMOJI") {
            Text("Restart", fontSize = 16.sp)
        }
    }
}

@Preview
@Composable
fun TestCaseRunPreview() {
    BLEScannerTheme {
        TestCaseRun(
            testCase = TestCaseId.SR_OW_1,
            selectedDevices = setOf(BluetoothDeviceData.sampleDevices.first().id),
            testRunnerState = "RUNNING $RUNNING_EMOJI",
            42,
            2034f,
            {
                // do nothing
            },
            {
                // do nothing
            }
        )
    }
}

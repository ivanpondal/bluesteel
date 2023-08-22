package com.example.blescanner.testrunner

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.blescanner.testrunner.services.TestRunner
import com.example.blescanner.ui.theme.BLEScannerTheme

@Composable
fun TestCaseRunView(testRunnerState: String) {
    Column {
        Text(testRunnerState, fontSize = 24.sp)
    }
}

@Preview
@Composable
fun TestCaseRunViewPreview() {
    BLEScannerTheme {
        TestCaseRunView(
            "RUNNING ${TestRunner.RUNNING_EMOJI}"
        )
    }
}

package com.example.blescanner.testrunner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blescanner.model.BluetoothDeviceData
import com.example.blescanner.testrunner.services.TestRunner
import com.example.blescanner.ui.theme.BLEScannerTheme

@Composable
fun TestCaseRunRow(testRunnerState: String, testDeviceId: String, packetsSent: Int) {
    Card {
        Column(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth())
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(testRunnerState, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(testDeviceId, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("⬆️ uplink: ", fontSize = 12.sp)
            Text("#️⃣ packets sent: $packetsSent", fontSize = 12.sp)
        }
    }
}

@Preview
@Composable
fun TestCaseRunViewPreview() {
    BLEScannerTheme {
        TestCaseRunRow(
            "RUNNING ${TestRunner.RUNNING_EMOJI}", BluetoothDeviceData.sampleDevices.first().id, 43
        )
    }
}

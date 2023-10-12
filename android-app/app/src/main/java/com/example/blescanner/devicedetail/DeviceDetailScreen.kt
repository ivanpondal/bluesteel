package com.example.blescanner.devicedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blescanner.model.BluetoothDeviceAdvertisement
import com.example.blescanner.model.BluetoothDeviceData
import com.example.blescanner.ui.theme.BLEScannerTheme

@Composable
fun DeviceDetail(device: BluetoothDeviceAdvertisement, onConnect: (deviceId: String) -> Unit) {
    Column {
        Column(
            Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = device.id,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.size(24.dp))
            Text(
                text = "Name: ${device.name ?: "<no name>"}"
            )
            Spacer(modifier = Modifier.size(24.dp))
            Text(
                text = "RSSI: ${device.rssi}"
            )
            Spacer(modifier = Modifier.size(24.dp))

            Text(
                text = "Advertised services",
                fontWeight = FontWeight.Bold
            )
        }
        LazyColumn(
            Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            items(device.services) {
                Card(
                    elevation = 2.dp, modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp)
                ) {
                    Text(
                        text = it.toString(),
                        Modifier.padding(8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1.0f))
        Button(
            onClick = { onConnect(device.id) },
            contentPadding = PaddingValues(64.dp, 12.dp, 64.dp, 12.dp),
            modifier = Modifier
                .align(alignment = CenterHorizontally)
                .padding(bottom = 16.dp),
        ) {
            Text("CONNECT", fontSize = 16.sp)
        }
    }
}

@Preview
@Composable
fun DeviceDetailPreview() {
    BLEScannerTheme {
        DeviceDetail(BluetoothDeviceData.sampleDevices.first()) { _ -> }
    }
}

@Preview
@Composable
fun DeviceWithAdvertisementsDetailPreview() {
    BLEScannerTheme {
        DeviceDetail(BluetoothDeviceData.sampleDevices[1]) { _ -> }
    }
}

package com.example.blescanner.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blescanner.model.BluetoothDevice
import com.example.blescanner.model.BluetoothDeviceData
import com.example.blescanner.ui.theme.BLEScannerTheme

@Composable
fun DeviceList(
    devices: List<BluetoothDevice>, onNavigateToDevice: (deviceId: String) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(devices, key = { it.id }) {
            DeviceRow(device = it, onNavigateToDevice = onNavigateToDevice)
        }
    }
}

@Preview
@Composable
fun DeviceListPreview() {
    BLEScannerTheme {
        DeviceList(devices = BluetoothDeviceData.sampleDevices) {}
    }
}

@Composable
fun DeviceRow(device: BluetoothDevice, onNavigateToDevice: (deviceId: String) -> Unit) {
    Card(modifier = Modifier.clickable { onNavigateToDevice(device.id) }) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(text = device.id, fontSize = 16.sp, fontWeight = FontWeight.ExtraLight)
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = device.name ?: "<no name>",
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.size(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "Advertised services:",
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.size(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(14.dp)
                        .background(color = MaterialTheme.colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = device.advertisements.size.toString(),
                        fontSize = 10.sp,
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center,
                    )
                }
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right,
                    text = "RSSI: ${device.rssi}",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun DeviceRowPreview() {
    BLEScannerTheme {
        DeviceRow(BluetoothDeviceData.sampleDevices.first()) {}
    }
}

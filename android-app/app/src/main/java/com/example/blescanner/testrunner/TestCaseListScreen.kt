package com.example.blescanner.testrunner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blescanner.model.BluetoothDeviceData
import com.example.blescanner.model.BluetoothScannedDevice
import com.example.blescanner.ui.theme.BLEScannerTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TestCaseList(
    connectedDevices: List<BluetoothScannedDevice>,
    selectedDevices: Map<String, Boolean>,
    onDeviceToggle: (deviceId: String) -> Unit
) {
    Column {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(connectedDevices, key = { it.id }) {
                Card(
                    elevation = 2.dp,
                    modifier = Modifier.toggleable(
                        role = Role.Switch,
                        value = selectedDevices[it.id] ?: false,
                        onValueChange = { _ -> onDeviceToggle(it.id) },
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(it.id, fontSize = 14.sp)
                        Spacer(Modifier.weight(1.0f))
                        Switch(checked = selectedDevices[it.id] ?: false, onCheckedChange = null)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1.0f))
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .height(42.dp)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Test case")
                    Divider(
                        Modifier
                            .fillMaxHeight()
                            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                            .width(2.dp)
                    )
                    Text(
                        "SR-OW-1",
                        color = MaterialTheme.colors.secondaryVariant,
                        fontWeight = FontWeight.Bold
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(onClick = { /* Handle refresh! */ }) {
                            Text("SR-OW-1")
                        }
                        DropdownMenuItem(onClick = { /* Handle settings! */ }) {
                            Text("Settings")
                        }
                        DropdownMenuItem(onClick = { /* Handle send feedback! */ }) {
                            Text("Send Feedback")
                        }
                    }
                }
            }

            Button(
                onClick = {},
                contentPadding = PaddingValues(64.dp, 12.dp, 64.dp, 12.dp),
                modifier = Modifier
                    .padding(bottom = 16.dp)
            ) {
                Text("RUN", fontSize = 16.sp)
            }
        }
    }
}

@Preview
@Composable
fun TestCaseListPreview() {
    BLEScannerTheme {
        TestCaseList(
            connectedDevices = BluetoothDeviceData.sampleDevices,
            selectedDevices = mapOf(BluetoothDeviceData.sampleDevices.first().id to true),
            onDeviceToggle = { _ -> }
        )
    }
}

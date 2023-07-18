package com.example.blescanner.testrunner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.blescanner.model.BluetoothScannedDevice

@Composable
fun TestCaseList(
    connectedDevices: List<BluetoothScannedDevice>,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(connectedDevices, key = { it.id }) {
            Text(text = it.id)
        }
    }
}

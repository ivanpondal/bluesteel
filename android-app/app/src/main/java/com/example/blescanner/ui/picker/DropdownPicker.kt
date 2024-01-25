package com.example.blescanner.ui.picker

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <K> DropdownPicker(
    label: String,
    selectionOptions: Map<K, String>,
    currentSelection: K,
    onSelection: (selection: K) -> Unit,
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
            Text(label)
            Divider(
                Modifier
                    .fillMaxHeight()
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                    .width(2.dp)
            )

            Text(
                selectionOptions.get(currentSelection) ?: "N/A",
                color = MaterialTheme.colors.secondaryVariant,
                fontWeight = FontWeight.Bold
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }) {
                selectionOptions.map {
                    DropdownMenuItem(onClick = {
                        onSelection(it.key)
                        expanded = false
                    }) {
                        Text(it.value)
                    }
                }
            }
        }
    }
}
package com.example.blescanner.testrunner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blescanner.model.BluetoothDeviceData
import com.example.blescanner.model.Identifiable
import com.example.blescanner.testrunner.model.TestCaseId
import com.example.blescanner.testrunner.model.TestRole
import com.example.blescanner.ui.picker.DropdownPicker
import com.example.blescanner.ui.theme.BLEScannerTheme

@Composable
fun TestCaseList(
    connectedDevices: List<Identifiable<String>>,
    selectedDevices: Set<String>,
    onDeviceToggle: (deviceId: String) -> Unit,
    selectedTestCase: TestCaseId,
    availableTestCases: List<TestCaseId>,
    onTestCaseSelection: (testCase: TestCaseId) -> Unit,
    selectedTestCaseRole: TestRole,
    onTestRoleSelection: (testRole: TestRole) -> Unit,
    selectedTestNodeIndex: UByte,
    onTestNodeIndexSelection: (testNodeIndex: UByte) -> Unit,
    onClickRun: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

        if (selectedTestCase === TestCaseId.SR_OW_1) {
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
                            value = selectedDevices.contains(it.id),
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
                            Switch(
                                checked = selectedDevices.contains(it.id),
                                onCheckedChange = null
                            )
                        }
                    }
                }
            }
        }

        if (selectedTestCase === TestCaseId.SR_OW_4) {
            DropdownPicker(
                label = "Role",
                selectionOptions = mapOf(TestRole.A to "Foreground", TestRole.B to "Background"),
                currentSelection = selectedTestCaseRole,
                onSelection = onTestRoleSelection
            )
        }

        if (selectedTestCase === TestCaseId.SR_OW_5) {
            DropdownPicker(
                label = "Role",
                selectionOptions = mapOf(
                    TestRole.A to "Sender",
                    TestRole.B to "Relay",
                    TestRole.C to "Receiver"
                ),
                currentSelection = selectedTestCaseRole,
                onSelection = onTestRoleSelection
            )

            when (selectedTestCaseRole) {
                TestRole.B -> {
                    DropdownPicker(
                        label = "Node Index",
                        selectionOptions = (0u..4u).associate { it.toUByte() to it.toString() },
                        currentSelection = selectedTestNodeIndex,
                        onSelection = onTestNodeIndexSelection
                    )
                }

                TestRole.C -> {
                    DropdownPicker(
                        label = "Node Index",
                        selectionOptions = (0u..4u).associate { it.toUByte() to it.toString() },
                        currentSelection = selectedTestNodeIndex,
                        onSelection = onTestNodeIndexSelection
                    )
                }

                else -> {}
            }
        }
        Spacer(modifier = Modifier.weight(1.0f))

        DropdownPicker(
            label = "Test case",
            selectionOptions = availableTestCases.associateWith { it.displayName },
            currentSelection = selectedTestCase,
            onSelection = onTestCaseSelection
        )

        Button(
            onClick = onClickRun,
            contentPadding = PaddingValues(64.dp, 12.dp, 64.dp, 12.dp),
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            Text("RUN", fontSize = 16.sp)
        }
    }
}

@Preview
@Composable
fun TestCaseListSrOw1Preview() {
    BLEScannerTheme {
        TestCaseList(
            connectedDevices = BluetoothDeviceData.sampleDevices,
            selectedDevices = setOf(BluetoothDeviceData.sampleDevices.first().id),
            onDeviceToggle = { _ -> },
            selectedTestCase = TestCaseId.SR_OW_1,
            availableTestCases = listOf(TestCaseId.SR_OW_1, TestCaseId.SR_OW_4),
            onTestCaseSelection = { _ -> },
            selectedTestCaseRole = TestRole.A,
            onTestRoleSelection = { _ -> },
            selectedTestNodeIndex = 0u,
            onTestNodeIndexSelection = {},
            onClickRun = { }
        )
    }
}

@Preview
@Composable
fun TestCaseListSrOw5SenderPreview() {
    BLEScannerTheme {
        TestCaseList(
            connectedDevices = BluetoothDeviceData.sampleDevices,
            selectedDevices = setOf(BluetoothDeviceData.sampleDevices.first().id),
            onDeviceToggle = { _ -> },
            selectedTestCase = TestCaseId.SR_OW_5,
            availableTestCases = listOf(TestCaseId.SR_OW_1, TestCaseId.SR_OW_5),
            onTestCaseSelection = { _ -> },
            selectedTestCaseRole = TestRole.A,
            onTestRoleSelection = { _ -> },
            selectedTestNodeIndex = 0u,
            onTestNodeIndexSelection = {},
            onClickRun = { }
        )
    }
}

@Preview
@Composable
fun TestCaseListSrOw5RelayPreview() {
    BLEScannerTheme {
        TestCaseList(
            connectedDevices = BluetoothDeviceData.sampleDevices,
            selectedDevices = setOf(BluetoothDeviceData.sampleDevices.first().id),
            onDeviceToggle = { _ -> },
            selectedTestCase = TestCaseId.SR_OW_5,
            availableTestCases = listOf(TestCaseId.SR_OW_1, TestCaseId.SR_OW_5),
            onTestCaseSelection = { _ -> },
            selectedTestCaseRole = TestRole.B,
            onTestRoleSelection = { _ -> },
            selectedTestNodeIndex = 0u,
            onTestNodeIndexSelection = {},
            onClickRun = { }
        )
    }
}

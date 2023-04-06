package com.example.blescanner

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.blescanner.model.BluetoothDevice
import com.example.blescanner.model.BluetoothDeviceData
import com.example.blescanner.ui.theme.BLEScannerTheme

const val REQUEST_ENABLE_BT: Int = 1
const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private val bleViewModel: BluetoothDevicesViewModel by viewModels()

    private val turnOnBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Log.d(TAG, "bluetooth activadooo")
            } else {
                Log.d(TAG, "bluetooth no se pudo activar :(")
            }
        }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                Log.d(TAG, "permiso concedido")
            } else {
                Log.d(TAG, "permiso no concedido")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BLEScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    val bluetoothDevices = bleViewModel.bluetoothDevices.collectAsState(
                        emptyList()
                    )

                    NavHost(navController = navController, startDestination = "scanner") {
                        composable("scanner") {
                            DeviceList(
                                devices = bluetoothDevices.value,
                                onNavigateToDevice = { deviceId ->
                                    navController.navigate(
                                        "device/$deviceId"
                                    )
                                })
                        }
                        composable("device/{deviceId}") { backStackEntry ->
                            val deviceId =
                                backStackEntry.arguments?.getString("deviceId") ?: "no id :("
                            val device =
                                bluetoothDevices.value.first { it.id == deviceId }
                            DeviceDetail(device = device, onConnect = { id ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                                        applicationContext,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    requestLocationPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                } else {
                                    bleViewModel.stopScan()
                                    bleViewModel.connectGatt(id)
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!bleViewModel.bluetoothEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            turnOnBluetooth.launch(enableBtIntent)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "No tiene permiso de bluetooth scan")
                return
            } else if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "No tiene permiso de fine location para el scan")

                requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                return
            }
            Log.d(TAG, "Start scanning")
            bleViewModel.startScan()
            bleViewModel.startAdvertisement()
        }
    }

}

@Composable
fun DeviceRow(device: BluetoothDevice, onNavigateToDevice: (deviceId: String) -> Unit) {
    Card(modifier = Modifier.clickable { onNavigateToDevice(device.id) }) {
        Column(
            modifier = Modifier
                .padding(10.dp)
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
fun DeviceDetail(device: BluetoothDevice, onConnect: (deviceId: String) -> Unit) {
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
            items(device.advertisements) {
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

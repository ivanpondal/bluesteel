package com.example.blescanner

import android.Manifest
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
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
                val bluetoothDevices = bleViewModel.bluetoothDevices.collectAsState()
                // A surface container using the 'background' color from the theme
                Log.d(TAG, "RENDERING BLUETOOTH DEVICES")
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DeviceList(devices = bluetoothDevices.value)
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
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
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
        }
    }

}

@Composable
fun DeviceRow(device: BluetoothDevice) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = device.id, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = device.name ?: "<no name>", fontSize = 12.sp)
            Text(modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right, text = device.rssi.toString(), fontSize = 14.sp)
        }
    }
}

@Preview
@Composable
fun DeviceRowPreview() {
    BLEScannerTheme {
        DeviceRow(BluetoothDeviceData.sampleDevices.first())
    }
}

@Composable
fun DeviceList(devices: List<BluetoothDevice>) {
    LazyColumn {
        items(devices, key = { it.id }) {
            DeviceRow(device = it)
        }
    }
}

@Preview
@Composable
fun DeviceListPreview() {
    BLEScannerTheme {
        DeviceList(devices = BluetoothDeviceData.sampleDevices)
    }
}
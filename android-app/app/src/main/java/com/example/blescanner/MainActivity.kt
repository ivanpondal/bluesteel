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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.blescanner.devicedetail.DeviceDetail
import com.example.blescanner.scanner.DeviceList
import com.example.blescanner.ui.theme.BLEScannerTheme
import kotlinx.coroutines.launch

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


        lifecycleScope.launch {
            bleViewModel.deviceConnectionEvent.flowWithLifecycle(
                lifecycle = lifecycle,
                Lifecycle.State.STARTED
            ).collect { Log.d(TAG, "recibí conexión de ${it.device}") }
        }
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

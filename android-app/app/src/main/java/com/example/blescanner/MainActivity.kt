package com.example.blescanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.blescanner.devicedetail.DeviceDetail
import com.example.blescanner.devicedetail.DeviceDetailViewModel
import com.example.blescanner.scanner.DeviceList
import com.example.blescanner.scanner.DeviceListViewModel
import com.example.blescanner.scanner.repository.ConnectedDeviceRepository
import com.example.blescanner.scanner.repository.ScannedDeviceRepository
import com.example.blescanner.scanner.service.BluetoothClientService
import com.example.blescanner.scanner.service.BluetoothScanner
import com.example.blescanner.testrunner.TestCaseList
import com.example.blescanner.testrunner.TestCaseListViewModel
import com.example.blescanner.testrunner.TestCaseRun
import com.example.blescanner.testrunner.TestCaseRunViewModel
import com.example.blescanner.testrunner.model.TestCaseId
import com.example.blescanner.ui.theme.BLEScannerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import java.util.Collections.emptyList

const val REQUEST_ENABLE_BT: Int = 1

class MainActivity : ComponentActivity() {
    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    private val bleViewModel: BluetoothAdvertiserViewModel by viewModels()
    private val bluetoothManager: BluetoothManager by lazy {
        application.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothScanner: BluetoothScanner by lazy {
        BluetoothScanner(bluetoothManager, MainScope())
    }
    private val bluetoothClientService: BluetoothClientService by lazy {
        BluetoothClientService(bluetoothManager, CoroutineScope(Dispatchers.IO), this)
    }

    private val scannedDeviceRepository: ScannedDeviceRepository by lazy {
        ScannedDeviceRepository(bluetoothScanner, MainScope())
    }

    private lateinit var connectedDeviceRepository: ConnectedDeviceRepository

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

        connectedDeviceRepository =
            ConnectedDeviceRepository(bluetoothClientService, MainScope())

        setContent {
            BLEScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    Scaffold(bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        Log.d(
                            TAG,
                            "current dest ${
                                currentDestination?.hierarchy?.map { it.route }?.toList()
                            }"
                        )
                        BottomNavigation {
                            BottomNavigationItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "scanner"
                                    )
                                },
                                selected = currentDestination?.hierarchy?.any { it.route == "scanner" || it.route == "devices/{deviceId}" }
                                    ?: false,
                                onClick = {
                                    navController.navigate("scanner") {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        // on the back stack as users select items
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when
                                        // reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                })
                            BottomNavigationItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.List,
                                        contentDescription = "test cases"
                                    )
                                },
                                selected = currentDestination?.hierarchy?.any { it.route == "testrunner" }
                                    ?: false,
                                onClick = {
                                    navController.navigate("testrunner") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                })
                        }
                    }) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "scanner",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("scanner") {
                                val deviceListViewModel: DeviceListViewModel by viewModels {
                                    DeviceListViewModel.provideFactory(
                                        scannedDeviceRepository
                                    )
                                }
                                DeviceList(
                                    devices = deviceListViewModel.scannedDevices.collectAsState(
                                        emptyList()
                                    ).value,
                                    onNavigateToDevice = { deviceId ->
                                        navController.navigate(
                                            "devices/$deviceId"
                                        )
                                    })
                            }
                            composable("devices/{deviceId}") { backStackEntry ->
                                val deviceDetailViewModel: DeviceDetailViewModel by viewModels {
                                    DeviceDetailViewModel.provideFactory(
                                        scannedDeviceRepository
                                    )
                                }
                                val deviceId =
                                    backStackEntry.arguments?.getString("deviceId") ?: "no id :("
                                val device = deviceDetailViewModel.getScannedDeviceById(deviceId)
                                    .collectAsState().value
                                DeviceDetail(device = device, onConnect = { id ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                                            applicationContext,
                                            Manifest.permission.BLUETOOTH_CONNECT
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        requestLocationPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                    } else {
                                        bluetoothScanner.stopScan()
                                        bluetoothClientService.connect(id)
                                    }
                                })
                            }

                            composable("testrunner") {
                                val testCaseListViewModel: TestCaseListViewModel by viewModels {
                                    TestCaseListViewModel.provideFactory(
                                        connectedDeviceRepository
                                    )
                                }
                                TestCaseList(
                                    connectedDevices = testCaseListViewModel.connectedDevices.collectAsState().value,
                                    selectedDevices = testCaseListViewModel.selectedDevices.collectAsState().value,
                                    onDeviceToggle = testCaseListViewModel::toggle,
                                    selectedTestCase = testCaseListViewModel.selectedTestCase.collectAsState().value,
                                    availableTestCases = TestCaseId.values().toList(),
                                    onTestCaseSelection = testCaseListViewModel::setTestCase,
                                    onClickRun = {
                                        navController.navigate(
                                            "testrunner/${testCaseListViewModel.selectedTestCase.value}?devices=${
                                                testCaseListViewModel.selectedDevices.value.joinToString(
                                                    ","
                                                )
                                            }"
                                        )
                                    }
                                )
                            }

                            composable("testrunner/{testCase}?devices={devices}",
                                arguments = listOf(
                                    navArgument("testCase") { type = NavType.StringType },
                                    navArgument("devices") { type = NavType.StringType }
                                )) { backStackEntry ->
                                val testCase = TestCaseId.valueOf(
                                    backStackEntry.arguments?.getString("testCase") ?: "N/A"
                                )
                                val devices =
                                    backStackEntry.arguments?.getString("devices")?.split(",")
                                        ?: listOf()
                                val owner = LocalLifecycleOwner.current

                                val testCaseRunViewModel: TestCaseRunViewModel by viewModels {
                                    TestCaseRunViewModel.provideFactory(
                                        connectedDeviceRepository,
                                        testCase,
                                        devices.toSet()
                                    )
                                }

                                DisposableEffect(devices, testCase, owner) {
                                    val observer = LifecycleEventObserver { _, event ->
                                        when (event) {
                                            Lifecycle.Event.ON_CREATE -> {
                                                testCaseRunViewModel.runTest()
                                            }

                                            else -> {
                                                // do nothing
                                            }
                                        }
                                    }

                                    // Add the observer to the lifecycle
                                    owner.lifecycle.addObserver(observer)
                                    onDispose {
                                        owner.lifecycle.removeObserver(observer)
                                    }
                                }
                                TestCaseRun(
                                    testCase = testCaseRunViewModel.testCase,
                                    selectedDevices = testCaseRunViewModel.devices,
                                    testRunnerState = testCaseRunViewModel.testRunnerState.collectAsState().value,
                                    testRunnerPacketsSent = testCaseRunViewModel.testRunnerPacketsSent.collectAsState().value,
                                    testRunnerBytesPerSecond = testCaseRunViewModel.testRunnerBytesPerSecond.collectAsState().value,
                                    onCopyResults = {},
                                    onRestart = { testCaseRunViewModel.runTest() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "No tiene permiso de bluetooth scan")
            return
        }
        bluetoothScanner.stopScan()
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
            bluetoothScanner.startScan()
            bleViewModel.startAdvertisement()
        }
    }
}

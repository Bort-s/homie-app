package com.example.homieapp

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.homieapp.core.ble.BTManager
import com.example.homieapp.core.model.Block
import com.example.homieapp.core.model.HomieMobile
import com.example.homieapp.core.ui.components.PrincipalText
import com.example.homieapp.core.ui.components.SecondaryText
import com.example.homieapp.core.ui.theme.HomieAppTheme
import com.example.homieapp.navigation.AppNavigation
import com.example.homieapp.navigation.AppRoutes

class MainActivity : ComponentActivity() {
    private lateinit var btManager: BTManager
    private var homieMobile by mutableStateOf(HomieMobile())

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
        }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btManager = BTManager(this)

        checkPermissions()

        setContent {
            HomieAppTheme {
                val foundDevices by btManager.foundDevices.collectAsState()
                val receivedData by btManager.receivedData.collectAsState()

                LaunchedEffect(receivedData) {
                    processReceivedData(receivedData)
                }

                HomieAppApp(
                    homieMobile = homieMobile,
                    foundDevices = foundDevices,
                    onStartScan = { btManager.startScan() },
                    onStopScan = { btManager.stopScan() },
                    onConnectDevice = { device ->
                        homieMobile = homieMobile.copy(
                            id = device.name?.removePrefix("HMMB") ?: "",
                            address = device.address,
                            register = true,
                            connected = true
                        )
                        btManager.connectToDevice(device)
                    }
                )
            }
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun processReceivedData(data: String) {
        if (data.isEmpty()) return

        val parts = data.split(":")
        if (parts.size != 2) return
        
        val key = parts[0]
        val value = parts[1].toIntOrNull() ?: return

        homieMobile = when (key) {
            "TEMP" -> homieMobile.copy(temperature = value)
            "HUM" -> homieMobile.copy(humidity = value)
            "AQI" -> homieMobile.copy(aqi = value)
            "NOX" -> homieMobile.copy(nox = value)
            "VOC" -> homieMobile.copy(voc = value)
            else -> homieMobile
        }

        homieMobile = homieMobile.copy()
    }
}

@Composable
fun HomieAppApp(
    homieMobile: HomieMobile,
    foundDevices: List<BluetoothDevice>,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (BluetoothDevice) -> Unit
) {
    val backStack = remember { mutableStateListOf<AppRoutes>(AppRoutes.HomeRoute) }

    val navigationColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF0055d4),
            selectedTextColor = Color(0xFF0055d4),
            unselectedIconColor = Color(0xFF7D7C7C),
            unselectedTextColor = Color(0xFF7D7C7C),
            indicatorColor = Color.Transparent,
        )
    )
    val suiteColors = NavigationSuiteDefaults.colors(
        navigationBarContainerColor = MaterialTheme.colorScheme.surface,
    )

    NavigationSuiteScaffold(
        navigationSuiteColors = suiteColors,
        navigationSuiteItems = {
            AppDestinations.entries.filter { it.showInBottomBar }.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            painterResource(destination.icon),
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = backStack.lastOrNull() == destination.route,
                    onClick = {
                        if (backStack.lastOrNull() != destination.route) {
                            backStack.clear()
                            backStack.add(destination.route)
                        }
                    },
                    colors = navigationColors,
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                AppNavigation(
                    homieMobile = homieMobile,
                    foundDevices = foundDevices,
                    onStartScan = onStartScan,
                    onStopScan = onStopScan,
                    onConnectDevice = onConnectDevice,
                    backStack = backStack
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
    val route: AppRoutes,
    val showInBottomBar: Boolean = true
) {
    HOME("Home", R.drawable.home, AppRoutes.HomeRoute),
    DEVICES("Devices", R.drawable.home_iot_device, AppRoutes.DevicesRoute),
    Alerts("Alerts", R.drawable.notification_important_24dp_1f1f1f_fill0_wght400_grad0_opsz24, AppRoutes.AlertsRoute),
    GUIDE("Guía", R.drawable.info, AppRoutes.GuideRoute, showInBottomBar = false),
    HOMIEMOBILE("Homie Mobile", R.drawable.homie_mobile, AppRoutes.HomieMobileRoute, showInBottomBar = false),
}

// Layout Composables
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoModalBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrincipalText("Acerca de", 24)
            SecondaryText("**que es homie**", 16)
        }
    }
}

@Composable
fun BlockGuide(block: Block) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surface)
        .padding(bottom = 16.dp, top = 16.dp)) {
        Row(horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 16.dp, bottom = 8.dp)
                .fillMaxWidth()
        ){
            PrincipalText(block.title, 24)
        }
        Text(
            text = block.content,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Justify
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun AlertCard(title: String, state: Int, value: String, color: Color, hour: String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(iconState[state]),
                    contentDescription = "Icono",
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(78.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Text(
                    text = hour,
                    color = Color(125, 125, 125),
                    fontSize = 24.sp,
                )
            }
        }
        SecondaryText(advice[state], 16, modifier = Modifier.padding(start = 100.dp, end = 16.dp))
        Text(
            text = value,
            color = color,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

val advice = arrayOf(
    "Todo parace estar en orden.",
    "La temperatura se encuentra arriba del rango recomendado",
    "La temperatura se encuentra abajo del rango recomendado",
    "La humedad se encuentra arriba del rango recomendado",
    "La humedad se encuentra abajo del rango recomendado",
    "La temperatura y humedad se encuentra en rangos no recomendado",
    "El nivel de calidad del aire se encuentra abajo del rango recomendado",
    "El nivel de calidad del aire es muy malo, tome medidas inmediatamente"
)

val domeState = intArrayOf(
    R.drawable.dom_e_esperanzado,
    R.drawable.dom_e_fuego,
    R.drawable.dom_e_congelado,
    R.drawable.afraid_dommy,
    R.drawable.afraid_dommy,
    R.drawable.afraid_dommy,
    R.drawable.dom_e_humo,
    R.drawable.dead_dommy
)

val iconState = intArrayOf(
    R.drawable.check,
    R.drawable.emergency_heat,
    R.drawable.mode_cool,
    R.drawable.humidity_high,
    R.drawable.humidity_low,
    R.drawable.warning,
    R.drawable.airwave,
    R.drawable.e911_emergency
)

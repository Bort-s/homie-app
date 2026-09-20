package com.example.homieapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.homieapp.core.ble.BTManager
import com.example.homieapp.core.model.Block
import com.example.homieapp.core.ui.theme.HomieAppTheme
import com.example.homieapp.features.guides.GuidesViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image

class MainActivity : ComponentActivity() {
    private lateinit var btManager: BTManager

    private var homieMobileState = mutableStateOf(HomieMobile())

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when (action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    homieMobileState.value = homieMobileState.value.copy(connected = true)
                    Log.d("BT_STATUS", "Device Connected")
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    homieMobileState.value = homieMobileState.value.copy(connected = false)
                    Log.d("BT_STATUS", "Device Disconnected")
                }
            }
        }
    }

    private fun checkBluetoothConnection() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true // BLUETOOTH is a normal permission below API 31
        }

        if (hasPermission) {
            try {
                val connectedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
                if (connectedDevices.isNotEmpty()) {
                    homieMobileState.value = homieMobileState.value.copy(connected = true)
                }
            } catch (e: SecurityException) {
                Log.e("BT_LOG", "SecurityException checking connection: ${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkBluetoothConnection()

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(bluetoothReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(bluetoothReceiver, filter)
        }

        btManager = BTManager(this) { deviceName, type, value ->
            Log.i("DATA_CHECK", "Incoming -> $type: $value")

            val current = homieMobileState.value
            homieMobileState.value = when (type) {
                "TEMP" -> current.copy(
                    temperature = value,
                    temperatureHistory = current.temperatureHistory.addAndTrim(value),
                    colorTemperature = colorTemperature(value),
                    state = getState(value, current.humidity, current.aqi)
                )
                "HUM" -> current.copy(
                    humidity = value,
                    humidityHistory = current.humidityHistory.addAndTrim(value),
                    colorHumidity = colorHumidity(value),
                    state = getState(current.temperature, value, current.aqi)
                )
                "AQI" -> current.copy(
                    aqi = value,
                    aqiHistory = current.aqiHistory.addAndTrim(value),
                    colorAQ = colorAQI(value),
                    state = getState(current.temperature, current.humidity, value)
                )
                else -> current
            }
        }

        setContent {
            HomieAppTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val allGranted = permissions.values.all { it }
                    if (allGranted) {
                        Log.d("BT_LOG", "Permissions granted by user.")
                        checkBluetoothConnection()
                    } else {
                        Log.e("BT_LOG", "Permissions denied.")
                    }
                }

                LaunchedEffect(Unit) {
                    val permissionsNeeded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
                    } else {
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                    }

                    permissionLauncher.launch(permissionsNeeded)
                }

                HomieAppApp(
                    homieMobile = homieMobileState.value,
                    onStartScan = { btManager.startScanning() },
                    onRegisterDevice = { newId ->
                        homieMobileState.value = homieMobileState.value.copy(
                            id = newId,
                            register = true
                        )
                        btManager.startScanning()
                    }
                )
            }
        }
    }
}

// @PreviewScreenSizes
@Composable
fun HomieAppApp(
    homieMobile: HomieMobile,
    onStartScan: () -> Unit,
    onRegisterDevice: (String) -> Unit
) {
    var guidePage by rememberSaveable { mutableIntStateOf(0) }

    // Colors
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

    // Navigation
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteColors = suiteColors,
        navigationSuiteItems = {
            AppDestinations.entries.filter { it.showInBottomBar }.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it },
                    colors = navigationColors,
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(homieMobile,
                    onNavigateToGuide = { currentDestination = AppDestinations.GUIDE })
                    AppDestinations.DEVICES -> DevicesScreen(
                        homieMobile,
                        onNavigateToHomieMobile = { currentDestination = AppDestinations.HOMIEMOBILE },
                        onRegisterDevice = { newId ->
                            onRegisterDevice(newId)
                        }
                    )
                    AppDestinations.HOMIEMOBILE -> HomieMobileScreen(homieMobile,
                        onNavigateToDevice = { currentDestination = AppDestinations.DEVICES },
                        onNavigateToGuide = { currentDestination = AppDestinations.GUIDE })
                AppDestinations.Alerts -> AlertsScreen(homieMobile)
                    AppDestinations.GUIDE -> GuideScreen(
                        guidePage,
                        onNavigateToHome = { currentDestination = AppDestinations.HOME },
                        addPage = { guidePage += 1 },
                        restPage = { guidePage -= 1 }
                    )
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
    val showInBottomBar: Boolean = true
) {
    HOME("Home", R.drawable.home),
    DEVICES("Devices", R.drawable.home_iot_device),
    Alerts("Alerts", R.drawable.notification_important_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
    GUIDE("Guía", R.drawable.info, showInBottomBar = false),
    HOMIEMOBILE("Homie Mobile", R.drawable.homie_mobile, showInBottomBar = false),
}

// Layout Composables
@Composable
fun HomeScreen(homieMobile: HomieMobile, onNavigateToGuide: () -> Unit) {
    var expandedInfo by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }

    val percentage: Float = ((500 - (homieMobile.aqi).toFloat().coerceIn(0f, 500f)) / 500)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxSize()
        ) {
            // Header
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(bottom = 12.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.happy_dommy),
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(80.dp),
                    contentDescription = "Dom-e Feliz")

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceEvenly) {
                        SecondaryText("Bienvenido", 16)
                        PrincipalText("Homie App", 32)
                        SecondaryText("", 16)
                    }

                    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        IconButton(onClick = { expandedInfo = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.info),
                                contentDescription = "Info"
                            )
                        }
                        DropdownMenu(
                            expanded = expandedInfo,
                            onDismissRequest = { expandedInfo = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Información") },
                                onClick = {
                                    expandedInfo = false
                                    showSheet = true
                                }
                            )
                        }
                        if (showSheet) {
                            InfoModalBottomSheet(onDismiss = { showSheet = false })
                        }
                    }
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 16.dp)) {
                        PrincipalText("Vista General", 24, modifier = Modifier.padding(bottom = 8.dp))
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp, top = 8.dp)) {
                            Column(
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(homieMobile.colorTemperature.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.device_thermostat),
                                        contentDescription = "Termostato",
                                        tint = homieMobile.colorTemperature,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                PrincipalText("${homieMobile.temperature}°C", 32)
                                SecondaryText("Temperatura", 16)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(homieMobile.colorHumidity.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.water_drop),
                                        contentDescription = "Humedad",
                                        tint = homieMobile.colorHumidity,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                PrincipalText("${homieMobile.humidity}%", 32)
                                SecondaryText("H. Relativa", 16)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .padding(top = 6.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(homieMobile.colorAQ.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.air),
                                    contentDescription = "Aire",
                                    tint = homieMobile.colorAQ,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Column(modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start
                                ) {
                                PrincipalText("Calidad del aire", 16)
                                SecondaryText("Exellent", 14)
                                SecondaryText("AQI: ${homieMobile.aqi}", 14)
                            }
                            Box(
                                modifier = Modifier
                                    .width(72.dp)
                                    .height(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(125, 125, 125))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(percentage)
                                        .clip(CircleShape)
                                        .background(homieMobile.colorAQ)
                                )
                            }
                        }
                    }
                }
                item {
                    Button(onClick = onNavigateToGuide,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0055d4).copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(2.dp, Color(0xFF0055d4)),
                        modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, top = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.menu_book),
                                contentDescription = "Guia",
                                modifier = Modifier.size(32.dp),
                                tint = Color(0xFF0055d4)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Consulta Nuestra Guia",
                                color = Color(0xFF0055d4),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp,
                            )
                        }
                    }
                }
                item {
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
                            Image(
                                painter = painterResource(R.drawable.dom_e_sabio),
                                contentDescription = "Dom-e",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(end = 8.dp)
                            )
                            PrincipalText("Dato ambiental del dia", 24)
                        }
                        SecondaryText(facts[1], 16, modifier = Modifier.padding(start = 16.dp, end = 16.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DevicesScreen(
    homieMobile: HomieMobile,
    onNavigateToHomieMobile: () -> Unit,
    onRegisterDevice: (String) -> Unit)  {
    var showHMIDMenu by remember { mutableStateOf(false) }
    var id by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(vertical = 16.dp, horizontal = 8.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)) {
                PrincipalText("Dispositivos", 24, modifier = Modifier.padding(vertical = 8.dp))
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(Modifier.fillMaxSize()) {
                    item {
                        DeviceCard(
                            name = homieMobile.name,
                            type = "Homie Mobile",
                            connected = homieMobile.connected,
                            R.drawable.homie_mobile,
                            homieMobile.register,
                            onClick = {
                                if (homieMobile.register) {
                                    onNavigateToHomieMobile()
                                    onRegisterDevice(id)
                                }
                                else showHMIDMenu = true
                            },
                            composables = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "${homieMobile.temperature}°C",
                                        color = homieMobile.colorTemperature,
                                        fontSize = 32.sp,
                                    )
                                    Text(
                                        text = "${homieMobile.humidity}%",
                                        color = homieMobile.colorHumidity,
                                        fontSize = 32.sp,
                                    )
                                    Text(
                                        text = "AQI: ${homieMobile.aqi}",
                                        color = homieMobile.colorAQ,
                                        fontSize = 32.sp,
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    item {

                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showHMIDMenu) {
            AlertDialog(
                onDismissRequest = {
                    showHMIDMenu = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showHMIDMenu = false
                            onRegisterDevice(id)
                        }) {
                        Text("Confirmar y Cerrar")
                    }
                },
                title = { Text("Registrar Dispositivo") },
                text = {
                    Column {
                        Text("Ingrese el ID del dispositivo")
                        OutlinedTextField(
                            value = id,
                            label = { Text("ID") },
                            onValueChange = { id = it },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                }
                            )
                        )
                    }
                }
            )
        }
    }
}


@Composable
fun HomieMobileScreen(homieMobile: HomieMobile, onNavigateToDevice: () -> Unit, onNavigateToGuide: () -> Unit)  {
    val connectionColor = if (homieMobile.connected) colorResource(R.color.green_homie) else colorResource(R.color.alert_color)
    val connectionStatus = if (homieMobile.connected) "Conectado" else "Desconectado"

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(vertical = 16.dp, horizontal = 8.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    IconButton(onClick = onNavigateToDevice) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Regresar",
                            tint = Color(0xFF0055d4),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),) {
                        PrincipalText("Homie Mobile", 24, modifier = Modifier.padding(start = 8.dp))
                        Box(
                            modifier = Modifier
                                .width(128.dp)
                                .clip(CircleShape)
                                .border(width = 2.dp, color = connectionColor, shape = CircleShape)
                                .background(connectionColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = connectionStatus,
                                color = connectionColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    MeasureCard(
                        homieMobile.temperatureHistory,
                        homieMobile.colorTemperature,
                        "Temperatura",
                        R.drawable.device_thermostat,
                        "°C"
                    )
                    MeasureCard(
                        homieMobile.humidityHistory,
                        homieMobile.colorHumidity,
                        "Humedad",
                        R.drawable.water_drop,
                        "%"
                    )
                    MeasureCard(
                        homieMobile.aqiHistory,
                        homieMobile.colorAQ,
                        "AQI",
                        R.drawable.air,
                        ""
                    )
                }
                item {
                    Column(modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)) {
                        Row(horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(start = 16.dp, bottom = 8.dp, top = 16.dp)
                                .fillMaxWidth()
                        ){
                            Image(
                                painter = painterResource(domeState[homieMobile.state]),
                                contentDescription = "Dom-e",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(end = 8.dp)
                            )
                            PrincipalText("Consejo de Dom-e", 24)
                        }
                        SecondaryText(advice[homieMobile.state], 20, modifier = Modifier.padding(start = 16.dp, end = 16.dp))
                        TextButton(onClick = onNavigateToGuide) {
                            Text(
                                text = "Consulta Nuestra Guia",
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// @Preview (showBackground = true)
@Composable
fun HomieMobilePreview() {
    HomieAppTheme {
        HomieMobileScreen(HomieMobile(), onNavigateToDevice = {}, onNavigateToGuide = {})
    }
}



@Composable
fun AlertsScreen(homieMobile: HomieMobile) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(vertical = 16.dp, horizontal = 8.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)) {
                PrincipalText("Historial de Alertas", 24, modifier = Modifier.padding(vertical = 8.dp))
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    AlertCard(
                        homieMobile.name,
                        homieMobile.state,
                        homieMobile.temperature.toString(),
                        colorTemperature(homieMobile.temperature),
                        "02:41"
                    )
                }
            }
        }
    }
}

@Composable
fun GuideScreen(guidePage: Int, onNavigateToHome: () -> Unit, addPage: () -> Unit, restPage: () -> Unit) {
    val viewmodel: GuidesViewModel = viewModel()
    val allGuides by viewmodel.listGuides.collectAsState()
    var currentGuide by remember { mutableIntStateOf(guidePage) }
    val guide = allGuides[currentGuide]

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxSize()
        ) {
            // Header
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                    .fillMaxWidth()) {
                    IconButton(onClick = onNavigateToHome
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Regresar",
                            tint = Color(0xFF0055d4),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        "Guia de Usuario",
                        color = Color(0xFF0055d4),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0055d4).copy(alpha = 0.1f))
                    ) {
                    Text(
                        "${guide.chapter} ● CAPITULO 0${guide.number}",
                        color = Color(0xFF0055d4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp)

                    )
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                item {
                    Text(
                        "Capitulo ${guide.number}: ",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 42.sp
                    )
                    Text(
                        guide.title,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 42.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(guide.blocks) { bloque ->
                    BlockGuide(bloque)
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = {
                            if (currentGuide > 0) {
                                restPage()
                                currentGuide -= 1
                            }
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color(0xFF525252)
                            ),
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_back_ios),
                                    contentDescription = "Regresar",
                                )
                                Text(
                                    "Regresar\nCapitulo",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                        Button(onClick = {
                            if (currentGuide < allGuides.size - 1) {
                                addPage()
                                currentGuide += 1
                            }
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0055d4),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            enabled = currentGuide < allGuides.size - 1,
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                                Text(
                                    "Siguiente\nCapitulo",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_forward_ios),
                                    contentDescription = "Siguiente",
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

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
        // Content
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



// Resource Composables
@Composable
fun PrincipalText(text: String, size: Int, modifier: Modifier = Modifier) {
    Text(text = text, color = MaterialTheme.colorScheme.primary, fontSize = size.sp, fontWeight = FontWeight.SemiBold, modifier = modifier)
}

@Composable
fun SecondaryText(text: String, size: Int, modifier: Modifier = Modifier) {
    Text(text = text, color = MaterialTheme.colorScheme.secondary, fontSize = size.sp, fontWeight = FontWeight.Normal, modifier = modifier)
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

@Composable
fun DeviceCard(name: String, type: String, connected: Boolean, icon: Int, registed: Boolean, onClick: () -> Unit, composables: @Composable () -> Unit, modifier: Modifier = Modifier) {
    val connectionColor = if (connected) colorResource(R.color.green_homie) else colorResource(R.color.alert_color)
    val connectionStatus = if (connected) "Conectado" else "Desconectado"


    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween)
            {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0055D4).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = "Icono",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF0055D4)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(128.dp)
                        .clip(CircleShape)
                        .border(width = 2.dp, color = connectionColor, shape = CircleShape)
                        .background(connectionColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = connectionStatus,
                        color = connectionColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp)
                    )
                }

            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start) {
                    PrincipalText(name, 24)
                    SecondaryText(type, 16)
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (registed) composables()
                    else {
                        SecondaryText("El dispositivo no esta registrado, haga click para registrarlo", 12)
                    }
                }
            }
        }
    }
}

@Composable
fun LayoutGraph(data: List<Int>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF0055D4),
    lineWidth: Dp = 4.dp
) {
    Canvas(modifier = modifier.padding(8.dp)) {
        // 1. Verificación de seguridad
        if (data.size < 2) return@Canvas

        val spacing = size.width / (data.size - 1) // Espacio horizontal entre puntos
        val maxVal = data.maxOrNull()?.toFloat() ?: 0f
        val minVal = data.minOrNull()?.toFloat() ?: 0f

        val path = Path()
        val points = mutableListOf<Offset>()

        for (i in data.indices) {
            val x = i * spacing
            val yPercentage = (data[i].toFloat() - minVal) / (maxVal - minVal)
            val y = size.height - (yPercentage * size.height)
            points.add(Offset(x, y))
        }

        for (i in 0 until points.size - 1) {
            if (i == 0) {
                path.moveTo(points[0].x, points[0].y)
            }

            val startPoint = points[i]
            val endPoint = points[i + 1]

            val smoothness = spacing / 2.5f

            val controlPoint1 = Offset(
                x = startPoint.x + smoothness,
                y = startPoint.y
            )
            val controlPoint2 = Offset(
                x = endPoint.x - smoothness,
                y = endPoint.y
            )

            path.cubicTo(
                controlPoint1.x, controlPoint1.y,
                controlPoint2.x, controlPoint2.y,
                endPoint.x, endPoint.y
            )
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = lineWidth.toPx(),

                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun MeasureCard(data: List<Int>, color: Color, name: String, icon: Int, symbol: String) {
    Column(modifier =
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(20.dp))
                    .background(color.copy(alpha = 0.2f))
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = "Icono",
                    modifier = Modifier.size(48.dp),
                    tint = color
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                PrincipalText(name, 32)
                PrincipalText("${data.lastOrNull()}$symbol", 32)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            LayoutGraph(
                data = data,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

// Composable Preview
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomieAppTheme {
        val homieMobile = HomieMobile(
            25,
            45,
            aqi = 67,
            state = 0,
            id = "000000",
            name = "Homie Mobile",
            register = false,
            connected = true
        )
        homieMobile.state = getState(homieMobile.temperature, homieMobile.humidity, homieMobile.aqi)
        HomeScreen(homieMobile, onNavigateToGuide = {})
    }
}

// @Preview (showBackground = true)
@Composable
fun GuideScreenPreview() {
    HomieAppTheme {
        GuideScreen(1, onNavigateToHome = {}, addPage = {}, restPage = {})
    }
}

// @Preview(showBackground = true)
@Composable
fun DevicesScreenPreview() {
    HomieAppTheme {
        val homieMobile = HomieMobile(
            22,
            40,
            aqi = 200,
            state = 0,
            id = "000001",
            name = "Homie Mobile",
            register = true,
            connected = true,
        )
        homieMobile.state = getState(homieMobile.temperature, homieMobile.humidity, homieMobile.aqi)
        DevicesScreen(homieMobile, onNavigateToHomieMobile = {}, onRegisterDevice = {})
    }
}


//@Preview(showBackground = true)
@Composable
fun AlertScreenPreview() {
    HomieAppTheme {
        val homieMobile = HomieMobile(
            22,
            40,
            aqi = 200,
            state = 0,
            id = "000001",
            name = "Homie Mobile",
            register = true,
            connected = true
        )
        homieMobile.state = getState(homieMobile.temperature, homieMobile.humidity, homieMobile.aqi)
        AlertsScreen(homieMobile)
    }
}

// Functions
fun colorTemperature(temperature: Int): Color {
    var r: Int
    var g: Int
    if ( temperature > 21) {
        if (temperature < 26) {
            g = 255
            r = 255 - 64 * (26 - temperature)
        } else {
            r = 255
            g = 255 - 51 * (temperature - 26)
        }
    } else {
        if (temperature > 16) {
            g = 255
            r = 255 - 64 * (temperature - 17)
        } else {
            r = 255
            g = 255 - 51 * (17 - temperature)
        }
    }
    if (r < 0) r = 0
    if (g < 0) g = 0

    return Color(r, g, 0)
}

fun colorHumidity(humidity: Int): Color {
    var r: Int
    var g: Int
    if ( humidity > 45) {
        if (humidity < 56) {
            g = 255
            r = 255 - 26 * (55 - humidity)
        } else {
            r = 255
            g = 255 - 17 * (humidity - 55)
        }
    } else {
        if (humidity > 35) {
            g = 255
            r = 255 - 26 * (humidity - 35)
        } else {
            r = 255
            g = 255 - 17 * (35 - humidity)
        }
    }
    if (r < 0) r = 0
    if (g < 0) g = 0

    return Color(r, g, 0)
}

fun colorAQI(AQI: Int): Color {
    var r: Int
    var g: Int = 0
    var b: Int = 0

    if (AQI < 76) {
        g = 255
        r = (AQI - 25) * (255/50)
    } else if (AQI < 201) {
        r = 255
        g = 255 - (AQI-76) * 255 / 100
    } else if (AQI < 300) {
        r = 67
        b = 115
    } else {
        r = 115
    }

    if (r < 0) r = 0
    if (g < 0) g = 0

    return Color(r, g, b)
}

fun getState(temperature: Int, humidity: Int, aqi: Int): Int {
    return if (aqi > 150) 7
    else if (aqi > 100) 6
    else if (temperature !in 16..28) {
        if (humidity !in 30..70) 5
        else if (temperature < 16) 2
        else 1
    }
    else if (humidity < 30) 4
    else if (humidity > 60) 3
    else 0
}

fun <T> List<T>.addAndTrim(element: T, maxSize: Int = 60): List<T> {
    val mutableList = this.toMutableList()
    mutableList.add(element)
    if (mutableList.size > maxSize) {
        mutableList.removeAt(0)
    }
    return mutableList.toList()
}

// Arrays

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

val facts = arrayOf(
    "¿Sabías que los bosques tienen su propia red social? A través de una red de hongos subterráneos llamada micorriza, los árboles se comunican, comparten nutrientes e incluso se advierten sobre plagas.",
    "A finales de los 80, el mundo se unió para prohibir los químicos que destruían la capa de ozono (Protocolo de Montreal). ¿El resultado? Se está recuperando tan bien que se espera que para el 2066 esté completamente sanada sobre la Antártida.",
    "Una ballena promedio captura la misma cantidad de CO2 que 1,000 árboles. Sus desechos fertilizan el fitoplancton, que a su vez absorbe el 40% de todo el dióxido de carbono producido en el mundo y genera más de la mitad del oxígeno que respiramos.",
    "En la última década, el costo de la energía solar ha caído un 89%. Hoy en día, en muchas partes del mundo, es más barato construir una nueva planta solar que seguir operando una de carbón ya existente.",
    "No hace falta ser vegano estricto para ayudar. Si una familia promedio en EE. UU. (o cualquier país con alto consumo de carne) redujera su consumo de carne roja a la mitad, sería el equivalente a quitar su auto de la carretera por 6 meses.",
    "Lugares como la isla de El Hierro en España o Tokelau en el Pacífico ya funcionan casi en su totalidad con energía limpia (viento, agua y sol).",
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

object NotificationHelper {
    const val CHANNEL_ID = "my_basic_channel"

    fun createNotificationChannel(context: Context) {
        val name = "Notificaciones de App"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun sendNotification(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.homie_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}

// Classes
data class HomieMobile(
    var temperature: Int = 0,
    var humidity: Int = 0,
    var aqi: Int = 0,
    var state: Int = 0,
    var id: String = "000000",
    var name: String = "Homie Mobile",
    var register: Boolean = false,
    var connected: Boolean = false,
    var colorTemperature: Color = Color.Unspecified,
    var colorHumidity: Color = Color.Unspecified,
    var colorAQ: Color = Color.Unspecified,
    var temperatureHistory: List<Int> = emptyList(),
    var humidityHistory: List<Int> = emptyList(),
    var aqiHistory: List<Int> = emptyList(),
)

class Activator(
    var id: String = "000000",
    var name: String = "Activator",
    var register: Boolean = false,
    var connected: Boolean = false,
)

class GetHomieMobileData {
    fun getLatest(vall: Int): Int {
        return vall + (-1..1).random()
    }
}

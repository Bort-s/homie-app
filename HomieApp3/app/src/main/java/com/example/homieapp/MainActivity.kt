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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.homieapp.core.ble.BTManager
import com.example.homieapp.core.model.Block
import com.example.homieapp.core.model.HomieMobile
import com.example.homieapp.core.ui.theme.HomieAppTheme
import com.example.homieapp.navigation.AppNavigation
import com.example.homieapp.navigation.AppRoutes

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

@Composable
fun HomieAppApp(
    homieMobile: HomieMobile,
    onStartScan: () -> Unit,
    onRegisterDevice: (String) -> Unit
) {
    // Navigation backstack for Nav3
    val backStack = remember { mutableStateListOf<AppRoutes>(AppRoutes.HomeRoute) }

    // Navigation Suite Colors
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
                            // Clear stack and navigate to the new root destination
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
                    onRegisterDevice = onRegisterDevice,
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
        modifier = Modifier.fillMaxWidth(),
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
        if (data.size < 2) return@Canvas

        val spacing = size.width / (data.size - 1)
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

            val controlPoint1 = Offset(x = startPoint.x + smoothness, y = startPoint.y)
            val controlPoint2 = Offset(x = endPoint.x - smoothness, y = endPoint.y)

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
    "¿Sabías que los bosques tienen su propia red social?...",
    "A finales de los 80, el mundo se unió para prohibir los químicos...",
    "Una ballena promedio captura la misma cantidad de CO2 que 1,000 árboles...",
    "En la última década, el costo de la energía solar ha caído un 89%...",
    "No hace falta ser vegano estricto para ayudar...",
    "Lugares como la isla de El Hierro en España..."
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

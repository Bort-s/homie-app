package com.example.homieapp.features.devicepreview

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homieapp.R
import com.example.homieapp.core.model.HomieMobile
import com.example.homieapp.core.ui.components.PrincipalText
import com.example.homieapp.core.ui.theme.HomieAppTheme

@SuppressLint("MissingPermission")
@Composable
fun DevicePreviewScreen(
    homieMobile: HomieMobile,
    foundDevices: List<BluetoothDevice>,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (BluetoothDevice) -> Unit,
    onNavigateToHomieMobile: () -> Unit
) {
    var showHMIDMenu by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(vertical = 16.dp, horizontal = 8.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)) {
                PrincipalText("Dispositivo", 24, modifier = Modifier.padding(vertical = 8.dp))
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(Modifier.fillMaxSize()) {
                    item {
                        DeviceCard(
                            name = homieMobile.name,
                            id = homieMobile.id,
                            connected = homieMobile.connected,
                            R.drawable.homie_mobile,
                            homieMobile.register,
                            onClick = {
                                if (homieMobile.register) {
                                    onNavigateToHomieMobile()
                                } else {
                                    showHMIDMenu = true
                                }
                            },
                            composables = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "${homieMobile.temperature}°C",
                                        color = homieMobile.temperatureColor,
                                        fontSize = 32.sp,
                                    )
                                    Text(
                                        text = "${homieMobile.humidity}%",
                                        color = homieMobile.humidityColor,
                                        fontSize = 32.sp,
                                    )
                                    Text(
                                        text = "AQI: ${homieMobile.aqi}",
                                        color = homieMobile.aqiColor,
                                        fontSize = 32.sp,
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showHMIDMenu) {
        DisposableEffect(Unit) {
            onStartScan()
            onDispose {
                onStopScan()
            }
        }

        AlertDialog(
            onDismissRequest = {
                showHMIDMenu = false
            },
            confirmButton = {
                TextButton(onClick = { showHMIDMenu = false }) {
                    Text("Cerrar")
                }
            },
            title = { Text("Dispositivos Encontrados") },
            text = {
                Column {
                    if (foundDevices.isEmpty()) {
                        Text("Buscando dispositivos...")
                    } else {
                        LazyColumn {
                            items(foundDevices) { device ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onConnectDevice(device)
                                            showHMIDMenu = false
                                        }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(text = device.name ?: "Desconocido", style = MaterialTheme.typography.bodyLarge)
                                    Text(text = device.address, style = MaterialTheme.typography.bodySmall)
                                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DevicePreviewScreenPreview() {
    HomieAppTheme {
        val homieMobile = HomieMobile(
            temperature = 22,
            humidity = 40,
            aqi = 200,
            id = "000002",
            name = "Homie Mobile",
            register = true,
            connected = true,
        )
        DevicePreviewScreen(
            homieMobile = homieMobile,
            foundDevices = emptyList(),
            onStartScan = {},
            onStopScan = {},
            onConnectDevice = {},
            onNavigateToHomieMobile = {}
        )
    }
}

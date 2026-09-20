package com.example.homieapp.features.devicepreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homieapp.DeviceCard
import com.example.homieapp.PrincipalText
import com.example.homieapp.R
import com.example.homieapp.core.model.HomieMobile
import com.example.homieapp.core.ui.theme.HomieAppTheme
import com.example.homieapp.getState

@Composable
fun DevicePreviewScreen(
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

@Preview(showBackground = true)
@Composable
fun DevicePreviewScreenPreview() {
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
        DevicePreviewScreen(homieMobile, onNavigateToHomieMobile = {}, onRegisterDevice = {})
    }
}
package com.example.homieapp.features.homiemobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homieapp.MeasureCard
import com.example.homieapp.PrincipalText
import com.example.homieapp.R
import com.example.homieapp.SecondaryText
import com.example.homieapp.advice
import com.example.homieapp.core.model.HomieMobile
import com.example.homieapp.core.ui.theme.HomieAppTheme
import com.example.homieapp.domeState

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

@Preview(showBackground = true)
@Composable
fun HomieMobilePreview() {
    HomieAppTheme {
        HomieMobileScreen(
            HomieMobile(),
            onNavigateToDevice = {},
            onNavigateToGuide = {}
        )
    }
}
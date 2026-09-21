package com.example.homieapp.features.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homieapp.InfoModalBottomSheet
import com.example.homieapp.R
import com.example.homieapp.core.model.HomieMobile
import com.example.homieapp.core.ui.components.PrincipalText
import com.example.homieapp.core.ui.components.SecondaryText
import com.example.homieapp.core.ui.theme.HomieAppTheme
import java.time.LocalDate

@Composable
fun HomeScreen(homieMobile: HomieMobile, onNavigateToGuide: () -> Unit) {
    var expandedInfo by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }

    val facts = stringArrayResource(id = R.array.facts)
    val day = LocalDate.now().dayOfWeek.value - 1

    val aqiPercentage: Float = ((500 - (homieMobile.aqi).toFloat().coerceIn(0f, 500f)) / 500)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxSize()
        ) {
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
                                        .background(homieMobile.temperatureColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.device_thermostat),
                                        contentDescription = "Termostato",
                                        tint = homieMobile.temperatureColor,
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
                                        .background(homieMobile.humidityColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.water_drop),
                                        contentDescription = "Humedad",
                                        tint = homieMobile.humidityColor,
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
                                    .background(homieMobile.aqiColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.air),
                                    contentDescription = "Aire",
                                    tint = homieMobile.aqiColor,
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
                                        .fillMaxWidth(aqiPercentage)
                                        .clip(CircleShape)
                                        .background(homieMobile.aqiColor)
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
                        val fact = facts.getOrElse(day) { "Consejo no disponible" }
                        SecondaryText(fact, 16, modifier = Modifier.padding(start = 16.dp, end = 16.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomieAppTheme {
        val homieMobile = HomieMobile(
            30,
            45,
            aqi = 67,
            id = "000000",
            name = "Homie Mobile",
            register = false,
            connected = true
        )
        HomeScreen(homieMobile, onNavigateToGuide = {})
    }
}
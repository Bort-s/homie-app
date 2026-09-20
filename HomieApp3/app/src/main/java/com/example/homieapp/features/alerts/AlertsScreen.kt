package com.example.homieapp.features.alerts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.homieapp.AlertCard
import com.example.homieapp.PrincipalText
import com.example.homieapp.colorTemperature
import com.example.homieapp.core.model.HomieMobile
import com.example.homieapp.core.ui.theme.HomieAppTheme
import com.example.homieapp.getState

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

@Preview(showBackground = true)
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
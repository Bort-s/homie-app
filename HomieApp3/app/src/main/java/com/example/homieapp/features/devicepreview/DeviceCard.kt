package com.example.homieapp.features.devicepreview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.homieapp.R
import com.example.homieapp.core.ui.components.PrincipalText
import com.example.homieapp.core.ui.components.SecondaryText

@Composable
fun DeviceCard(name: String, id: String, connected: Boolean, icon: Int, registed: Boolean, onClick: () -> Unit, composables: @Composable () -> Unit, modifier: Modifier = Modifier) {
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
                    SecondaryText(id, 16)
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

@Preview(showBackground = true)
@Composable
fun DeviceCardPreview() {
    DeviceCard(
        name = "Homie Mobile",
        id = "123456",
        connected = true,
        icon = R.drawable.device_thermostat,
        registed = true,
        onClick = {},
        composables = {
            Text(text = "24°C", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    )
}

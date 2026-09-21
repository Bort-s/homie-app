package com.example.homieapp.features.homiemobile

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.homieapp.LayoutGraph
import com.example.homieapp.R
import com.example.homieapp.core.model.HomieMobile
import com.example.homieapp.core.ui.components.PrincipalText
import com.example.homieapp.core.ui.theme.HomieAppTheme

@Composable
fun MeasureCard(
    title: String,
    color: Color,
    icon: Int,
    symbol: String = ""
) {
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
                PrincipalText(title, 32)
                // PrincipalText("${data.lastOrNull()}$symbol", 32)
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Preview(showBackground = true)
@Composable
fun MeasureCardPreview() {
    HomieAppTheme {
        MeasureCard(
            "Temperatura",
            HomieMobile.colorTemperature(24),
            R.drawable.device_thermostat,
            "°C"
        )
    }
}
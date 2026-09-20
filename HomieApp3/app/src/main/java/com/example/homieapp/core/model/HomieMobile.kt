package com.example.homieapp.core.model

import androidx.compose.ui.graphics.Color

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
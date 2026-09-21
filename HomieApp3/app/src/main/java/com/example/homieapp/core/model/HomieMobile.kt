package com.example.homieapp.core.model

import androidx.compose.ui.graphics.Color

data class HomieMobile(
    var temperature: Int = 0,
    var humidity: Int = 0,
    var aqi: Int = 0,
    var nox: Int = 0,
    var voc: Int = 0,
    var id: String = "",
    var name: String = "Homie Mobile",
    var register: Boolean = false,
    var connected: Boolean = false,
    var address: String = ""
) {
    val temperatureColor = colorTemperature(temperature)
    val humidityColor = colorHumidity(humidity)
    val aqiColor = colorAQI(aqi)
    val state = getState(temperature, humidity, aqi)

    companion object {
        fun colorTemperature(temperature: Int): Color {
            var r: Int
            var g: Int
            if (temperature > 21) {
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
            if (humidity > 45) {
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
                r = (AQI - 25) * (255 / 50)
            } else if (AQI < 201) {
                r = 255
                g = 255 - (AQI - 76) * 255 / 100
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
            } else if (humidity < 30) 4
            else if (humidity > 60) 3
            else 0
        }
    }
}
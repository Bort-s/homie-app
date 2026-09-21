package com.example.homieapp.core.ble

data class BleModel (
    val deviceName: String = "",
    var temperature: Int = 0,
    var humidity: Int = 0,
    var aqi: Int = 0,
    var nox: Int = 0,
    var voc: Int = 0,
    var time: String = "",
)
package com.example.chis.model

data class SimulatedDeviceNode(
    val id: String,
    val devHardwareName: String,
    var name: String,
    val type: String,
    val rssi: String,
    var isBound: Boolean = false,
    var boundPlantName: String = "",
    var moisture: String = "60%",
    var temp: String = "24.8°C",
    var light: String = "3200",
    var npk: String = "880 NPK"
)
package com.example.chis.services

import com.example.chis.model.SimulatedDeviceNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object HuaweiCloudService {
    const val HW_AK = "HPUAW7NO99BVZZGSICTI"
    const val HW_SK = "jb0SuenfbldK2czYRtNuyxvlc5roiwWYE3yNihLs"

    const val DEVICE1_SHADOW_URL = "https://e1144d64d1.st1.iotda-app.cn-north-4.myhuaweicloud.com:443/v5/iot/8fa081c708324f619974bd5c2701f42d/devices/6a5bb330cbb0cf6bb9707167_SmartPot_01/shadow"
    const val DEVICE2_SHADOW_URL = "https://e1144d64d1.st1.iotda-app.cn-north-4.myhuaweicloud.com:443/v5/iot/8fa081c708324f619974bd5c2701f42d/devices/6a5bb330cbb0cf6bb9707167_PlantSensor_02/shadow"

    /**
     * 异步请求华为云 IAM Token
     */
    suspend fun fetchHwToken(): String = withContext(Dispatchers.IO) {
        try {
            val tokenUrl = "https://iam.cn-north-4.myhuaweicloud.com/v3/auth/tokens"
            val jsonBody = JSONObject().apply {
                put("auth", JSONObject().apply {
                    put("identity", JSONObject().apply {
                        put("methods", JSONArray().apply { put("hw_ak_sk") })
                        put("hw_ak_sk", JSONObject().apply {
                            put("access", JSONObject().apply { put("key", HW_AK) })
                            put("secret", JSONObject().apply { put("key", HW_SK) })
                        })
                    })
                    put("scope", JSONObject().apply {
                        put("project", JSONObject().apply { put("name", "cn-north-4") })
                    })
                })
            }

            val conn = (URL(tokenUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
            }

            conn.outputStream.use { it.write(jsonBody.toString().toByteArray()) }

            var token = ""
            if (conn.responseCode == 201 || conn.responseCode == 200) {
                token = conn.getHeaderField("X-Subject-Token") ?: conn.getHeaderField("x-subject-token") ?: ""
            }
            conn.disconnect()
            token
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 异步获取单条设备阴影并直接更新对应的设备对象字段
     */
    suspend fun fetchDeviceShadow(
        url: String,
        deviceIndex: Int,
        hwToken: String,
        availableDevices: List<SimulatedDeviceNode>
    ) = withContext(Dispatchers.IO) {
        if (hwToken.isEmpty()) return@withContext
        try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("X-Auth-Token", hwToken)
                connectTimeout = 8000
                readTimeout = 8000
            }

            if (conn.responseCode == 200) {
                val resStr = conn.inputStream.bufferedReader().use { it.readText() }
                val response = JSONObject(resStr)
                val shadowArr = response.optJSONArray("shadow")
                if (shadowArr != null) {
                    for (i in 0 until shadowArr.length()) {
                        val props = shadowArr.getJSONObject(i).optJSONObject("reported")?.optJSONObject("properties")
                        if (props != null && deviceIndex < availableDevices.size) {
                            withContext(Dispatchers.Main) {
                                val dev = availableDevices[deviceIndex]
                                props.optString("moisture").takeIf { it.isNotEmpty() && it != "null" }?.let { dev.moisture = "$it%" }
                                props.optString("temperature").takeIf { it.isNotEmpty() && it != "null" }?.let { dev.temp = "$it°C" }
                                props.optString("light").takeIf { it.isNotEmpty() && it != "null" }?.let { dev.light = it }
                                props.optString("npk").takeIf { it.isNotEmpty() && it != "null" }?.let { dev.npk = "$it NPK" }
                            }
                        }
                    }
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 批量更新所有已知设备的 Shadow 状态
     */
    suspend fun fetchAllDeviceShadows(hwToken: String, availableDevices: List<SimulatedDeviceNode>) {
        fetchDeviceShadow(DEVICE1_SHADOW_URL, 0, hwToken, availableDevices)
        fetchDeviceShadow(DEVICE2_SHADOW_URL, 1, hwToken, availableDevices)
    }
}
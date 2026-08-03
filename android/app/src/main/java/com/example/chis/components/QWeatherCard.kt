package com.example.chis.components

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.chis.R
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.zip.GZIPInputStream

// ====== QWeather API 数据模型 (1:1 映射 ArkTS 接口) ======

data class DailyForecastNode(
    var date: String,
    var dayText: String,
    var icon: String,
    var tempMax: String,
    var tempMin: String,
    var isToday: Boolean
)

data class StyledGridItemParams(
    val iconRes: Int,
    val valStr: String,
    val label: String,
    val isAlert: Boolean = false
)

@Composable
fun QWeatherCard(
    currentCityName: String,
    currentCityLocation: String,
    isDarkMode: Boolean,
    isEnglish: Boolean,
    onCityNameChange: (String) -> Unit,
    onOpenCitySheet: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var weatherTemp by remember { mutableStateOf("28") }
    var weatherFeelsLike by remember { mutableStateOf("28") }
    var weatherText by remember { mutableStateOf("多云") }
    var weatherMinMax by remember { mutableStateOf("27° / 30°") }
    var weatherRain by remember { mutableStateOf("0.0") }
    var weatherWind by remember { mutableStateOf("4") }
    var weatherHumidity by remember { mutableStateOf("74") }
    var weatherPressure by remember { mutableStateOf("1013") }
    var weatherWindDir by remember { mutableStateOf("东南风") }
    var weatherCloud by remember { mutableStateOf("25") }
    var weatherSunTime by remember { mutableStateOf("05:43/19:10") }

    var weeklyForecast by remember {
        mutableStateOf(
            listOf(
                DailyForecastNode("1", "周一", "🌧️", "32°", "28°", false),
                DailyForecastNode("2", "周二", "🌧️", "29°", "27°", false),
                DailyForecastNode("3", "周三", "🌧️", "28°", "27°", false),
                DailyForecastNode("4", "周四", "🌧️", "28°", "26°", false),
                DailyForecastNode("5", "周五", "🌧️", "30°", "27°", false),
                DailyForecastNode("6", "周六", "⛈️", "30°", "28°", true),
                DailyForecastNode("7", "周日", "⛅", "29°", "28°", false)
            )
        )
    }

    val qWeatherKey = "04e6c11eab65489aa966a7f0bd83e6d6"
    val apiHost = "ju5b676n2n.re.qweatherapi.com"

    // 解包 GZIP / Plain Text HTTP 响应辅助函数
    fun getResponseBody(conn: HttpURLConnection): String {
        val rawStream = if (conn.responseCode == 200) conn.inputStream else conn.errorStream ?: return ""
        val stream = if ("gzip".equals(conn.contentEncoding, ignoreCase = true)) {
            GZIPInputStream(rawStream)
        } else {
            rawStream
        }
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    // 辅助：更新星期标题
    fun updateWeekDayTitles() {
        val daysCn = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        val daysEn = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val updated = weeklyForecast.mapIndexed { i, item ->
            if (!item.date.contains("-")) {
                item.copy(dayText = if (isEnglish) daysEn[i % 7] else daysCn[i % 7])
            } else {
                item
            }
        }
        weeklyForecast = updated
    }

    // ---------- API 请求管线 ----------
    fun fetchQWeatherNow(loc: String, useFallback: Boolean = false) {
        scope.launch(Dispatchers.IO) {
            val host = if (useFallback) "devapi.qweather.com" else apiHost
            val urlStr = "https://$host/v7/weather/now?location=${URLEncoder.encode(loc, "UTF-8")}&key=$qWeatherKey&lang=zh"
            try {
                val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                    setRequestProperty("Accept-Encoding", "gzip")
                }
                val resStr = getResponseBody(conn)
                conn.disconnect()

                if (resStr.isNotEmpty()) {
                    val resObj = JSONObject(resStr)
                    if (resObj.optString("code") == "200") {
                        val nowObj = resObj.optJSONObject("now")
                        if (nowObj != null) {
                            withContext(Dispatchers.Main) {
                                weatherTemp = nowObj.optString("temp", "28")
                                weatherFeelsLike = nowObj.optString("feelsLike", weatherTemp)
                                weatherText = nowObj.optString("text", "多云")
                                weatherHumidity = nowObj.optString("humidity", "74")
                                weatherRain = nowObj.optString("precip", "0.0")
                                weatherWind = nowObj.optString("windSpeed", "4")
                                weatherPressure = nowObj.optString("pressure", "1013")
                                weatherWindDir = nowObj.optString("windDir", if (isEnglish) "SE Wind" else "东南风")
                                val cloudVal = nowObj.optString("cloud", "")
                                weatherCloud = if (cloudVal.isNotEmpty()) cloudVal else nowObj.optString("vis", "20")
                            }
                            return@launch
                        }
                    }
                }
                if (!useFallback) fetchQWeatherNow(loc, true)
            } catch (e: Exception) {
                if (!useFallback) fetchQWeatherNow(loc, true)
            }
        }
    }

    fun fetchQWeatherForecast(loc: String, useFallback: Boolean = false) {
        scope.launch(Dispatchers.IO) {
            val host = if (useFallback) "devapi.qweather.com" else apiHost
            val urlStr = "https://$host/v7/weather/7d?location=${URLEncoder.encode(loc, "UTF-8")}&key=$qWeatherKey&lang=zh"
            try {
                val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                    setRequestProperty("Accept-Encoding", "gzip")
                }
                val resStr = getResponseBody(conn)
                conn.disconnect()

                if (resStr.isNotEmpty()) {
                    val resObj = JSONObject(resStr)
                    if (resObj.optString("code") == "200") {
                        val dailyArr = resObj.optJSONArray("daily")
                        if (dailyArr != null && dailyArr.length() > 0) {
                            val todayDaily = dailyArr.getJSONObject(0)
                            val minMaxStr = "${todayDaily.optString("tempMin")}° / ${todayDaily.optString("tempMax")}°"
                            val sunStr = "${todayDaily.optString("sunrise")}/${todayDaily.optString("sunset")}"

                            val daysCn = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
                            val daysEn = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val todayStr = sdf.format(Calendar.getInstance().time)

                            val nodesMap = arrayOfNulls<DailyForecastNode>(7)
                            val sdfParse = SimpleDateFormat("yyyy-MM-dd", Locale.US)

                            for (i in 0 until minOf(dailyArr.length(), 7)) {
                                val item = dailyArr.getJSONObject(i)
                                val fxDate = item.optString("fxDate")
                                val dateObj = sdfParse.parse(fxDate)
                                val cal = Calendar.getInstance().apply {
                                    if (dateObj != null) time = dateObj
                                }
                                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                                val monIndex = (dayOfWeek + 5) % 7
                                val isToday = (i == 0) || (fxDate == todayStr)

                                val dayTitle = if (isEnglish) daysEn[monIndex] else (if (isToday) "今天" else daysCn[monIndex])
                                val textDay = item.optString("textDay", "")
                                val icon = when {
                                    textDay.contains("雷") -> "⛈️"
                                    textDay.contains("雨") -> "🌧️"
                                    textDay.contains("雪") -> "❄️"
                                    textDay.contains("阴") || textDay.contains("云") -> "⛅"
                                    else -> "☀️"
                                }

                                nodesMap[monIndex] = DailyForecastNode(
                                    date = fxDate,
                                    dayText = dayTitle,
                                    icon = icon,
                                    tempMax = "${item.optString("tempMax")}°",
                                    tempMin = "${item.optString("tempMin")}°",
                                    isToday = isToday
                                )
                            }

                            val dailyList = mutableListOf<DailyForecastNode>()
                            for (i in 0 until 7) {
                                nodesMap[i]?.let { dailyList.add(it) }
                            }

                            withContext(Dispatchers.Main) {
                                weatherMinMax = minMaxStr
                                if (sunStr.contains("/")) weatherSunTime = sunStr
                                if (dailyList.isNotEmpty()) weeklyForecast = dailyList
                            }
                            return@launch
                        }
                    }
                }
                if (!useFallback) fetchQWeatherForecast(loc, true)
            } catch (e: Exception) {
                if (!useFallback) fetchQWeatherForecast(loc, true)
            }
        }
    }

    fun fetchCityNameFromQWeather(loc: String, useFallback: Boolean = false) {
        scope.launch(Dispatchers.IO) {
            val host = if (useFallback) "geoapi.qweather.com" else apiHost
            val urlStr = "https://$host/v2/city/lookup?location=${URLEncoder.encode(loc, "UTF-8")}&key=$qWeatherKey&lang=zh"
            var targetLoc = loc
            try {
                val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                    setRequestProperty("Accept-Encoding", "gzip")
                }
                val resStr = getResponseBody(conn)
                conn.disconnect()

                if (resStr.isNotEmpty()) {
                    val resObj = JSONObject(resStr)
                    if (resObj.optString("code") == "200") {
                        val locationArr = resObj.optJSONArray("location")
                        if (locationArr != null && locationArr.length() > 0) {
                            val locItem = locationArr.getJSONObject(0)
                            var nameStr = locItem.optString("adm2", locItem.optString("name", locItem.optString("adm1", "")))
                            if (nameStr.endsWith("市") || nameStr.endsWith("区") || nameStr.endsWith("县")) {
                                nameStr = nameStr.substring(0, nameStr.length - 1)
                            }
                            if (nameStr.isNotEmpty()) {
                                val finalCityName = nameStr
                                withContext(Dispatchers.Main) {
                                    onCityNameChange(finalCityName)
                                }
                            }
                            targetLoc = locItem.optString("id", targetLoc)
                        }
                    }
                } else if (!useFallback) {
                    fetchCityNameFromQWeather(loc, true)
                    return@launch
                }
            } catch (e: Exception) {
                if (!useFallback) {
                    fetchCityNameFromQWeather(loc, true)
                    return@launch
                }
            }

            fetchQWeatherNow(targetLoc)
            fetchQWeatherForecast(targetLoc)
        }
    }

    fun executeQWeatherPipeline(location: String, cityName: String) {
        if (cityName != "自动定位" && !cityName.contains("定位") && !cityName.contains("Locating")) {
            onCityNameChange(cityName)
        }
        fetchCityNameFromQWeather(location)
    }

    fun requestLocationAndWeather() {
        onCityNameChange(if (isEnglish) "Locating..." else "定位中...")
        val hasFinePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val timerJob = scope.launch {
            delay(3500)
            if (currentCityName.contains("定位") || currentCityName.contains("Locating")) {
                withContext(Dispatchers.Main) {
                    onCityNameChange(if (isEnglish) "Locating..." else "定位中...")
                }
            }
        }

        if (hasFinePermission || hasCoarsePermission) {
            scope.launch(Dispatchers.IO) {
                var foundLoc: Location? = null
                try {
                    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                    foundLoc = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                        ?: locationManager.getLastKnownLocation(android.location.LocationManager.PASSIVE_PROVIDER)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (foundLoc != null) {
                    timerJob.cancel()
                    val locStr = String.format(Locale.US, "%.2f,%.2f", foundLoc.longitude, foundLoc.latitude)
                    fetchCityNameFromQWeather(locStr)
                } else {
                    withContext(Dispatchers.Main) {
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                            .addOnSuccessListener { location: Location? ->
                                timerJob.cancel()
                                if (location != null) {
                                    val locStr = String.format(Locale.US, "%.2f,%.2f", location.longitude, location.latitude)
                                    fetchCityNameFromQWeather(locStr)
                                } else {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                                        if (lastLoc != null) {
                                            val locStr = String.format(Locale.US, "%.2f,%.2f", lastLoc.longitude, lastLoc.latitude)
                                            fetchCityNameFromQWeather(locStr)
                                        } else {
                                            onCityNameChange(if (isEnglish) "Locating..." else "定位中...")
                                        }
                                    }.addOnFailureListener {
                                        onCityNameChange(if (isEnglish) "Locating..." else "定位中...")
                                    }
                                }
                            }
                            .addOnFailureListener {
                                timerJob.cancel()
                                onCityNameChange(if (isEnglish) "Locating..." else "定位中...")
                            }
                    }
                }
            }
        } else {
            timerJob.cancel()
            onCityNameChange(if (isEnglish) "Locating..." else "定位中...")
        }
    }

    LaunchedEffect(isEnglish) {
        updateWeekDayTitles()
    }

    LaunchedEffect(currentCityLocation) {
        if (currentCityLocation == "auto") {
            requestLocationAndWeather()
        } else if (currentCityLocation.isNotEmpty()) {
            executeQWeatherPipeline(currentCityLocation, currentCityName)
        }
    }

    // ---------- UI 构建 (1:1 转换 ArkTS UI 结构) ----------
    val pagerState = rememberPagerState(pageCount = { 2 })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(0xFF1E5F41), RoundedCornerShape(20.dp))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page == 0) {
                // 第一页：今日精简核心气象
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp)
                ) {
                    // 城市定位选择 Pill 胶囊
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                                .clickable { onOpenCitySheet() }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "📍 $currentCityName",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "▼",
                                fontSize = 8.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // 主气象大字号展示
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = weatherTemp,
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "°",
                                    fontSize = 32.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 14.dp)
                                )
                            }
                            Text(
                                text = weatherMinMax,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }

                        val weatherIcon = when {
                            weatherText.contains("雷") -> "⛈️"
                            weatherText.contains("雨") -> "🌧️"
                            weatherText.contains("阴") || weatherText.contains("云") -> "⛅"
                            else -> "☀️"
                        }
                        Text(
                            text = weatherIcon,
                            fontSize = 54.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 底部 3 指标 Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StyledGridItem(
                            modifier = Modifier.weight(1f),
                            params = StyledGridItemParams(
                                iconRes = R.drawable.humidity,
                                valStr = "$weatherHumidity%",
                                label = if (isEnglish) "HUMIDITY" else "湿度"
                            )
                        )
                        StyledGridItem(
                            modifier = Modifier.weight(1f),
                            params = StyledGridItemParams(
                                iconRes = R.drawable.pressure,
                                valStr = "$weatherPressure hPa",
                                label = if (isEnglish) "PRESSURE" else "气压"
                            )
                        )
                        StyledGridItem(
                            modifier = Modifier.weight(1f),
                            params = StyledGridItemParams(
                                iconRes = R.drawable.dogwane,
                                valStr = weatherWindDir,
                                label = if (isEnglish) "WIND DIR." else "风向"
                            )
                        )
                    }
                }
            } else {
                // 第二页：周气象预测
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                                .clickable { onOpenCitySheet() }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "📍 $currentCityName",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "▼",
                                fontSize = 8.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = if (isEnglish) "7-DAY FORECAST" else "本周 7 天天气预报",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }

                    // 7 天预报水平分布
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weeklyForecast.forEach { node ->
                            WeekForecastDayNode(node)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 底部 3 指标 Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StyledGridItem(
                            modifier = Modifier.weight(1f),
                            params = StyledGridItemParams(
                                iconRes = R.drawable.cloud,
                                valStr = "$weatherCloud%",
                                label = if (isEnglish) "CLOUD" else "云量"
                            )
                        )
                        StyledGridItem(
                            modifier = Modifier.weight(1f),
                            params = StyledGridItemParams(
                                iconRes = R.drawable.wind,
                                valStr = "$weatherWind km/h",
                                label = if (isEnglish) "WIND SPEED" else "风速"
                            )
                        )
                        StyledGridItem(
                            modifier = Modifier.weight(1f),
                            params = StyledGridItemParams(
                                iconRes = R.drawable.sun,
                                valStr = weatherSunTime,
                                label = if (isEnglish) "SUNRISE/SET" else "日出日落"
                            )
                        )
                    }
                }
            }
        }

        // DotIndicator 页面指示器 (1:1 转换 ArkTS DotIndicator.dot())
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .width(30.dp)
                .height(5.dp)
        ) {
            val totalPages = 2
            val spacing = 4.dp.toPx()
            var xOffset = 0f

            for (i in 0 until totalPages) {
                val isSelected = pagerState.currentPage == i
                val width = if (isSelected) 14.dp.toPx() else 5.dp.toPx()
                val height = 5.dp.toPx()
                val color = if (isSelected) Color.White else Color.White.copy(alpha = 0.35f)

                drawRoundRect(
                    color = color,
                    topLeft = Offset(xOffset, 0f),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())
                )
                xOffset += width + spacing
            }
        }
    }
}

@Composable
fun WeekForecastDayNode(node: DailyForecastNode) {
    Column(
        modifier = Modifier
            .background(
                color = if (node.isToday) Color.White.copy(alpha = 0.14f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (node.isToday) 1.dp else 0.dp,
                color = if (node.isToday) Color.White.copy(alpha = 0.25f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(top = 4.dp, bottom = 4.dp, start = 3.dp, end = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = node.dayText,
            fontSize = 10.sp,
            fontWeight = if (node.isToday) FontWeight.Bold else FontWeight.Medium,
            color = if (node.isToday) Color.White else Color.White.copy(alpha = 0.75f)
        )

        Text(
            text = node.icon,
            fontSize = 13.sp
        )

        Text(
            text = node.tempMax,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = node.tempMin,
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun StyledGridItem(
    modifier: Modifier = Modifier,
    params: StyledGridItemParams
) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = if (params.isAlert) Color(0x4DE9AA32) else Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = params.iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = params.valStr,
            fontSize = if (params.valStr.length > 8) 10.5.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Text(
            text = params.label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
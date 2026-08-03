package com.example.chis.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chis.R
import com.example.chis.model.SimulatedDeviceNode
import com.example.chis.services.MetricStatus
import com.example.chis.services.TelemetryService
import java.util.Calendar
import java.util.Locale

// ---------- 1. 内部历史数据模型 ----------
private data class HistoryPoint(
  val time: String,
  var moisture: Int,
  var temp: Float,
  var light: Int,
  var npk: Int
)

private data class DeviceAnalytics(
  val moistureHistory: MutableList<HistoryPoint>,
  val moisture7dHistory: MutableList<HistoryPoint>,
  val moisture30dHistory: MutableList<HistoryPoint>,
  var moistureAvg: String,
  var currentMoisture: String,
  var lightPeak: String,
  var tempAvg: String,
  var dryCycle: String,
  var sunlightHours: String,
  var nextWatering: String,
  var growthScore: String
)

@Composable
fun TrendsTab(
  isDarkMode: Boolean = false,
  isEnglish: Boolean = false,
  availableDevices: List<SimulatedDeviceNode>
) {
  var selectedDeviceIndex by remember { mutableIntStateOf(0) }
  var selectedTimeRange by remember { mutableIntStateOf(0) } // 0: 24h, 1: 7d, 2: 30d

  val timeRangeLabels = remember { listOf("24小时", "近 7 天", "近 30 天") }

  // ---------- 2. 预置模板数据 ----------
  val deviceDataMap = remember {
    listOf(
      DeviceAnalytics(
        moistureHistory = mutableListOf(
          HistoryPoint("22:00", 62, 22.5f, 0, 880),
          HistoryPoint("01:00", 60, 21.8f, 120, 880),
          HistoryPoint("04:00", 58, 24.2f, 2400, 875),
          HistoryPoint("07:00", 75, 26.5f, 5200, 870),
          HistoryPoint("10:00", 72, 25.0f, 1800, 868),
          HistoryPoint("13:00", 68, 23.4f, 50, 865),
          HistoryPoint("实时", 65, 24.8f, 3200, 880)
        ),
        moisture7dHistory = mutableListOf(
          HistoryPoint("07-13", 68, 23.0f, 4200, 890),
          HistoryPoint("07-14", 65, 24.1f, 4500, 885),
          HistoryPoint("07-15", 61, 23.8f, 3800, 880),
          HistoryPoint("07-16", 78, 25.2f, 5100, 875),
          HistoryPoint("07-17", 72, 24.5f, 4800, 870),
          HistoryPoint("07-18", 67, 24.0f, 4300, 868),
          HistoryPoint("今天", 65, 24.8f, 3200, 880)
        ),
        moisture30dHistory = mutableListOf(
          HistoryPoint("4周前", 70, 22.8f, 4100, 900),
          HistoryPoint("3周前", 64, 23.5f, 4600, 890),
          HistoryPoint("2周前", 69, 24.2f, 4800, 880),
          HistoryPoint("上周", 66, 24.0f, 4300, 872),
          HistoryPoint("本周", 65, 24.8f, 3200, 880)
        ),
        moistureAvg = "65%",
        currentMoisture = "65%",
        lightPeak = "5200 LUX",
        tempAvg = "24.8°C",
        dryCycle = "水分极佳",
        sunlightHours = "6.2 小时",
        nextWatering = "后天 10:00",
        growthScore = "98 分 · 生长极佳"
      ),
      DeviceAnalytics(
        moistureHistory = mutableListOf(
          HistoryPoint("22:00", 45, 24.0f, 0, 650),
          HistoryPoint("01:00", 43, 23.2f, 300, 645),
          HistoryPoint("04:00", 40, 26.0f, 3600, 640),
          HistoryPoint("07:00", 38, 28.5f, 6500, 630),
          HistoryPoint("10:00", 55, 27.0f, 2100, 625),
          HistoryPoint("13:00", 48, 25.2f, 0, 622),
          HistoryPoint("实时", 42, 26.0f, 4500, 620)
        ),
        moisture7dHistory = mutableListOf(
          HistoryPoint("07-13", 52, 25.0f, 5800, 680),
          HistoryPoint("07-14", 48, 25.5f, 6100, 670),
          HistoryPoint("07-15", 44, 26.2f, 6300, 660),
          HistoryPoint("07-16", 60, 24.8f, 5900, 650),
          HistoryPoint("07-17", 53, 25.8f, 6200, 640),
          HistoryPoint("07-18", 47, 26.5f, 6400, 630),
          HistoryPoint("今天", 42, 26.0f, 4500, 620)
        ),
        moisture30dHistory = mutableListOf(
          HistoryPoint("4周前", 55, 24.5f, 5600, 710),
          HistoryPoint("3周前", 50, 25.2f, 6000, 690),
          HistoryPoint("2周前", 48, 25.8f, 6200, 670),
          HistoryPoint("上周", 53, 26.0f, 6100, 650),
          HistoryPoint("本周", 42, 26.0f, 4500, 620)
        ),
        moistureAvg = "45%",
        currentMoisture = "42%",
        lightPeak = "6500 LUX",
        tempAvg = "26.0°C",
        dryCycle = "偏干缺水",
        sunlightHours = "7.5 小时",
        nextWatering = "明天 14:00",
        growthScore = "85 分 · 需补水"
      )
    )
  }

  // ---------- 3. 辅助算法 ----------
  fun getRollingTimestamps(): List<String> {
    val curHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val timestamps = mutableListOf<String>()
    for (i in 6 downTo 1) {
      val h = (curHour - i * 3 + 24) % 24
      timestamps.add(String.format(Locale.US, "%02d:00", h))
    }
    timestamps.add("实时")
    return timestamps
  }

  fun calculateGrowthScore(mStr: String, tStr: String, lStr: String, nStr: String): String {
    var score = 100
    val m = TelemetryService.getMoistureDiagnostic(mStr)
    val t = TelemetryService.getTempDiagnostic(tStr)
    val l = TelemetryService.getLightDiagnostic(lStr)
    val n = TelemetryService.getNpkDiagnostic(nStr)

    if (m.status != MetricStatus.NORMAL) score -= 25
    if (t.status != MetricStatus.NORMAL) score -= 15
    if (l.status != MetricStatus.NORMAL) score -= 15
    if (n.status != MetricStatus.NORMAL) score -= 20

    return when {
      score >= 90 -> "$score 分 · 生长极佳"
      score >= 70 -> "$score 分 · 状态良好"
      score >= 50 -> "$score 分 · 偏旱缺肥"
      else -> "$score 分 · 需紧急关照"
    }
  }

  fun getMoistureStatusCleanText(valStr: String): String {
    val num = valStr.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: return "水分极佳"
    return when {
      num < 30 -> "干旱缺水"
      num > 75 -> "积水过涝"
      else -> "水分极佳"
    }
  }

  fun getMoistureStatusColor(valStr: String): Color {
    val num = valStr.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: return Color(0xFF34C759)
    return when {
      num < 30 -> Color(0xFFFF9500)
      num > 75 -> Color(0xFFFF3B30)
      else -> Color(0xFF34C759)
    }
  }

  fun getNextWateringCleanText(valStr: String): String {
    val num = valStr.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: return "后天 10:00"
    return when {
      num < 20 -> "立即透浇"
      num < 35 -> "今天 18:00"
      num < 50 -> "明天 10:00"
      num < 70 -> "后天 10:00"
      else -> "4天后 10:00"
    }
  }

  val boundList = remember(availableDevices) { availableDevices.filter { it.isBound } }

  fun getActiveDevice(): SimulatedDeviceNode {
    val safeIdx = selectedDeviceIndex.coerceIn(0, (boundList.size - 1).coerceAtLeast(0))
    return if (boundList.isNotEmpty()) boundList[safeIdx] else SimulatedDeviceNode("", "", "智能设备", "", "")
  }

  fun getActiveAnalytics(): DeviceAnalytics {
    if (boundList.isEmpty()) return deviceDataMap[0]
    val safeIdx = selectedDeviceIndex.coerceIn(0, boundList.size - 1)
    val activeDev = boundList[safeIdx]
    val baseData = deviceDataMap[safeIdx % deviceDataMap.size]

    val timestamps = getRollingTimestamps()
    for (i in baseData.moistureHistory.indices) {
      if (i < timestamps.size) {
        baseData.moistureHistory[i] = baseData.moistureHistory[i].copy(time = timestamps[i])
      }
    }

    val realM = activeDev.moisture.replace("[^0-9.]".toRegex(), "").toFloatOrNull()
    val realT = activeDev.temp.replace("[^0-9.]".toRegex(), "").toFloatOrNull()
    val realL = activeDev.light.replace("[^0-9.]".toRegex(), "").toFloatOrNull()

    if (realM != null) {
      val mInt = realM.toInt()
      baseData.moistureHistory.lastOrNull()?.moisture = mInt
      baseData.moisture7dHistory.lastOrNull()?.moisture = mInt
      baseData.moisture30dHistory.lastOrNull()?.moisture = mInt
      baseData.currentMoisture = "$mInt%"
    }
    if (realT != null) {
      baseData.moistureHistory.lastOrNull()?.temp = realT
      baseData.moisture7dHistory.lastOrNull()?.temp = realT
      baseData.moisture30dHistory.lastOrNull()?.temp = realT
      baseData.tempAvg = String.format(Locale.US, "%.1f°C", realT)
    }
    if (realL != null) {
      val lInt = realL.toInt()
      baseData.moistureHistory.lastOrNull()?.light = lInt
      baseData.moisture7dHistory.lastOrNull()?.light = lInt
      baseData.moisture30dHistory.lastOrNull()?.light = lInt
      baseData.lightPeak = "$lInt LUX"
    }

    baseData.growthScore = calculateGrowthScore(activeDev.moisture, activeDev.temp, activeDev.light, activeDev.npk)
    return baseData
  }

  fun getActiveHistoryPoints(baseData: DeviceAnalytics): List<HistoryPoint> {
    return when (selectedTimeRange) {
      1 -> baseData.moisture7dHistory
      2 -> baseData.moisture30dHistory
      else -> baseData.moistureHistory
    }
  }

  // ---------- 4. 调色板 ----------
  val bgColor = if (isDarkMode) Color(0xFF000000) else Color(0xFFF2F2F7)
  val cardBgColor = if (isDarkMode) Color(0xFF1C1C1E) else Color.White
  val mainTextColor = if (isDarkMode) Color.White else Color(0xFF1C1C1E)
  val subTextColor = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8E8E93)
  val cardBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.03f)

  // ---------- 5. UI View 绘制 ----------
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(bgColor)
  ) {
    // 1. Apple-Style Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 12.dp)
    ) {
      Text(
        text = if (isEnglish) "Plant Data" else "养护数据",
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = mainTextColor
      )
      Text(
        text = if (isEnglish) "24h Real-Time Ecosystem & Telemetry" else "24 小时实时体征与生态监测",
        fontSize = 12.sp,
        color = subTextColor,
        modifier = Modifier.padding(top = 4.dp)
      )
    }

    if (boundList.isEmpty()) {
      // 暂无设备空状态
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.flower_pot), // ✅ 已替换：app.media.flower_pot
          contentDescription = "Empty",
          colorFilter = ColorFilter.tint(if (isDarkMode) Color(0xFF58B582) else Color(0xFF1E5F41)),
          modifier = Modifier.size(64.dp)
        )

        Box(modifier = Modifier.height(16.dp))

        Text(
          text = if (isEnglish) "No Bound Smart Devices" else "暂无已绑定设备的数据",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = if (isDarkMode) Color.White else Color(0xFF333333)
        )

        Box(modifier = Modifier.height(8.dp))

        Text(
          text = if (isEnglish) "Please add and bind a smart device on the Home tab first." else "请先在【主页】绑定智能设备，即可解锁 24 小时实时生态数据图表与健康报告。",
          fontSize = 13.sp,
          color = Color(0xFF8E8E93),
          textAlign = TextAlign.Center,
          lineHeight = 18.sp,
          modifier = Modifier.padding(horizontal = 32.dp)
        )
      }
    } else {
      // 有设备 - 主数据滚动卡片
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // 2. 多设备横向 Pill 切换器
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          itemsIndexed(boundList) { index, dev ->
            val isSelected = selectedDeviceIndex == index
            val activePillBg = if (isDarkMode) Color(0xFF34C759) else Color(0xFF1E5F41)
            val inactivePillBg = if (isDarkMode) Color(0xFF1C1C1E) else Color.White
            val activeBorder = if (isDarkMode) Color(0xFF34C759) else Color(0xFF1E5F41)
            val inactiveBorder = if (isDarkMode) Color(0xFF3A3A3C) else Color(0x40224A32)

            Row(
              modifier = Modifier
                .background(
                  color = if (isSelected) activePillBg else inactivePillBg,
                  shape = RoundedCornerShape(20.dp)
                )
                .border(
                  width = 1.5.dp,
                  color = if (isSelected) activeBorder else inactiveBorder,
                  shape = RoundedCornerShape(20.dp)
                )
                .clickable { selectedDeviceIndex = index }
                .padding(horizontal = 16.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Image(
                painter = painterResource(id = R.drawable.flower_pot), // ✅ 已替换：app.media.flower_pot
                contentDescription = "Device Pot",
                colorFilter = ColorFilter.tint(
                  if (isSelected) Color.White else (if (isDarkMode) Color(0xFF58B582) else Color(0xFF1E5F41))
                ),
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = dev.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else (if (isDarkMode) Color(0xFFE5E5EA) else Color(0xFF1E5F41))
              )
            }
          }
        }

        Box(modifier = Modifier.height(14.dp))

        // 3. Apple Health Segmented Control (时间范围切换)
        Box(
          modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(if (isDarkMode) Color(0xFF1C1C1E) else Color(0xFFEFEFF4), RoundedCornerShape(14.dp))
            .padding(3.dp)
        ) {
          val animatedOffsetX by animateFloatAsState(
            targetValue = selectedTimeRange * 0.3333f,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
            label = "SegmentOffset"
          )

          // 浮动滑块
          Box(
            modifier = Modifier
              .fillMaxWidth(0.3333f)
              .height(30.dp)
              .align(Alignment.CenterStart)
              .padding(horizontal = 0.dp)
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .shadow(4.dp, RoundedCornerShape(11.dp), clip = false)
                .background(if (isDarkMode) Color(0xFF2C2C2E) else Color.White, RoundedCornerShape(11.dp))
            )
          }

          Row(modifier = Modifier.fillMaxWidth()) {
            timeRangeLabels.forEachIndexed { idx, label ->
              val isSelected = selectedTimeRange == idx
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(30.dp)
                  .clickable { selectedTimeRange = idx },
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = label,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) {
                    if (isDarkMode) Color.White else Color(0xFF1C1C1E)
                  } else {
                    if (isDarkMode) Color.White.copy(alpha = 0.45f) else Color(0xFF8E8E93)
                  }
                )
              }
            }
          }
        }

        Box(modifier = Modifier.height(14.dp))

        // 4. Hero Card 1 - 💧 土壤湿度概览
        val activeAnalytics = getActiveAnalytics()
        val historyPoints = getActiveHistoryPoints(activeAnalytics)
        val activeDevice = getActiveDevice()

        Column(
          modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(cardBgColor, RoundedCornerShape(20.dp))
            .border(1.dp, cardBorderColor, RoundedCornerShape(20.dp))
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Image(
                  painter = painterResource(id = R.drawable.humidity), // ✅ 已替换：app.media.humidity
                  contentDescription = "Moisture Icon",
                  colorFilter = ColorFilter.tint(getMoistureStatusColor(activeDevice.moisture)),
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = when (selectedTimeRange) {
                    1 -> "7天土壤湿度演变"
                    2 -> "30天月度湿度周期"
                    else -> "土壤湿度概览"
                  },
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = subTextColor
                )
              }
              Text(
                text = activeDevice.moisture,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = mainTextColor
              )
            }
          }

          // 柱状图条排布
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
          ) {
            historyPoints.forEach { pt ->
              Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "${pt.moisture}%",
                  fontSize = 9.sp,
                  color = subTextColor
                )

                Box(
                  modifier = Modifier
                    .height(86.dp)
                    .width(18.dp)
                    .background(
                      if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color(0xFFF2F2F7),
                      RoundedCornerShape(6.dp)
                    ),
                  contentAlignment = Alignment.BottomCenter
                ) {
                  val barColor = when {
                    pt.moisture > 70 -> Color(0xFF30B0C7)
                    pt.moisture < 30 -> Color(0xFFFF9500)
                    else -> Color(0xFF34C759)
                  }
                  val pct = (pt.moisture.coerceIn(8, 100) / 100f)

                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .fillMaxHeight(pct)
                      .background(barColor, RoundedCornerShape(6.dp))
                  )
                }

                Text(
                  text = pt.time,
                  fontSize = 9.5.sp,
                  color = subTextColor
                )
              }
            }
          }
        }

        Box(modifier = Modifier.height(14.dp))

        // 5. Apple Weather Style Bento Grid (日光照射 & 环境温度)
        Row(
          modifier = Modifier.fillMaxWidth(0.9f),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // ☀️ 日光照射便当盒卡片
          Column(
            modifier = Modifier
              .weight(1f)
              .background(cardBgColor, RoundedCornerShape(18.dp))
              .border(1.dp, cardBorderColor, RoundedCornerShape(18.dp))
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Image(
                painter = painterResource(id = R.drawable.brightness), // ✅ 已替换：app.media.brightness
                contentDescription = "Light Icon",
                colorFilter = ColorFilter.tint(Color(0xFFFFCC00)),
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "日光照射",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = subTextColor
              )
            }

            Text(
              text = "${activeDevice.light} LUX",
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = mainTextColor
            )

            // 迷你黄色 Sparkline 柱状图
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.Bottom
            ) {
              historyPoints.forEach { pt ->
                val pct = (pt.light / 6500f).coerceIn(0.12f, 1.0f)
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .background(
                      if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color(0xFFF2F2F7),
                      RoundedCornerShape(3.dp)
                    ),
                  contentAlignment = Alignment.BottomCenter
                ) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .fillMaxHeight(pct)
                      .background(Color(0xFFFFCC00), RoundedCornerShape(3.dp))
                  )
                }
              }
            }
          }

          // 🌡️ 环境温度便当盒卡片
          Column(
            modifier = Modifier
              .weight(1f)
              .background(cardBgColor, RoundedCornerShape(18.dp))
              .border(1.dp, cardBorderColor, RoundedCornerShape(18.dp))
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Image(
                painter = painterResource(id = R.drawable.temperature), // ✅ 已替换：app.media.temperature
                contentDescription = "Temp Icon",
                colorFilter = ColorFilter.tint(Color(0xFFFF9500)),
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "环境温度",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = subTextColor
              )
            }

            Text(
              text = activeDevice.temp,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = mainTextColor
            )

            // 迷你橙色 Sparkline 柱状图
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.Bottom
            ) {
              historyPoints.forEach { pt ->
                val pct = (pt.temp / 35f).coerceIn(0.12f, 1.0f)
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .background(
                      if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color(0xFFF2F2F7),
                      RoundedCornerShape(3.dp)
                    ),
                  contentAlignment = Alignment.BottomCenter
                ) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .fillMaxHeight(pct)
                      .background(Color(0xFFFF9500), RoundedCornerShape(3.dp))
                  )
                }
              }
            }
          }
        }

        Box(modifier = Modifier.height(14.dp))

        // 6. Apple Health Inset Grouped List - 数据报告
        Column(
          modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(cardBgColor, RoundedCornerShape(20.dp))
            .border(1.dp, cardBorderColor, RoundedCornerShape(20.dp))
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Image(
                painter = painterResource(id = R.drawable.tip), // ✅ 已替换：app.media.tip
                contentDescription = "Tip Icon",
                colorFilter = ColorFilter.tint(if (isDarkMode) Color(0xFF34C759) else Color(0xFF1E5F41)),
                modifier = Modifier.size(15.dp)
              )
              Text(
                text = "数据报告",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = mainTextColor
              )
            }

            Text(
              text = activeAnalytics.growthScore,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = getMoistureStatusColor(activeDevice.moisture)
            )
          }

          Column(modifier = Modifier.fillMaxWidth()) {
            // Inset Row 1: 土壤干湿状态
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Image(
                  painter = painterResource(id = R.drawable.humidity), // ✅ 已替换：app.media.humidity
                  contentDescription = "Moisture Icon",
                  colorFilter = ColorFilter.tint(getMoistureStatusColor(activeDevice.moisture)),
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = "土壤干湿状态",
                  fontSize = 13.5.sp,
                  color = if (isDarkMode) Color(0xFFE5E5EA) else Color(0xFF1C1C1E)
                )
              }

              Text(
                text = getMoistureStatusCleanText(activeDevice.moisture),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = getMoistureStatusColor(activeDevice.moisture)
              )
            }

            HorizontalDivider(
              color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
            )

            // Inset Row 2: 日均有效光合时长
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Image(
                  painter = painterResource(id = R.drawable.brightness), // ✅ 已替换：app.media.brightness
                  contentDescription = "Brightness Icon",
                  colorFilter = ColorFilter.tint(Color(0xFF34C759)),
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = "日均有效光合时长",
                  fontSize = 13.5.sp,
                  color = if (isDarkMode) Color(0xFFE5E5EA) else Color(0xFF1C1C1E)
                )
              }

              Text(
                text = activeAnalytics.sunlightHours,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF34C759)
              )
            }

            HorizontalDivider(
              color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
            )

            // Inset Row 3: 智能预测浇水提醒
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Image(
                  painter = painterResource(id = R.drawable.alarm), // ✅ 已替换：app.media.alarm
                  contentDescription = "Alarm Icon",
                  colorFilter = ColorFilter.tint(getMoistureStatusColor(activeDevice.moisture)),
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = "智能预测浇水提醒",
                  fontSize = 13.5.sp,
                  color = if (isDarkMode) Color(0xFFE5E5EA) else Color(0xFF1C1C1E)
                )
              }

              Text(
                text = getNextWateringCleanText(activeDevice.moisture),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = getMoistureStatusColor(activeDevice.moisture)
              )
            }
          }
        }

        Box(modifier = Modifier.height(80.dp))
      }
    }
  }
}
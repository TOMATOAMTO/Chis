package com.example.chis.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.chis.R
import com.example.chis.model.SimulatedDeviceNode
import com.example.chis.services.MetricDiagnostic
import com.example.chis.services.NotificationService
import com.example.chis.services.OverallHealth
import com.example.chis.services.TelemetryService
import kotlinx.coroutines.delay
import java.util.Locale

@SuppressLint("MissingPermission")
@Composable
fun DeviceCard(
  device: SimulatedDeviceNode,
  isDarkMode: Boolean = false,
  lightUnit: String = "LUX"
) {
  val context = LocalContext.current

  // ---------- 1. 业务诊断与通知触发 ----------
  val mDiag: MetricDiagnostic = remember(device.moisture) {
    TelemetryService.getMoistureDiagnostic(device.moisture)
  }

  val tDiag: MetricDiagnostic = remember(device.temp) {
    TelemetryService.getTempDiagnostic(device.temp)
  }

  val lDiag: MetricDiagnostic = remember(device.light) {
    TelemetryService.getLightDiagnostic(device.light)
  }

  val nDiag: MetricDiagnostic = remember(device.npk) {
    TelemetryService.getNpkDiagnostic(device.npk)
  }

  val health: OverallHealth = remember(device.moisture, device.temp, device.light, device.npk) {
    val h = TelemetryService.getOverallDeviceHealth(
      device.moisture,
      device.temp,
      device.light,
      device.npk
    )
    if (h.statusText == "注意预警" || h.statusText == "养护关注") {
      val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
      } else {
        true
      }

      if (hasPermission) {
        try {
          NotificationService.publishAnomalyPush(
            context = context,
            title = "植物养护预警 · ${device.name}",
            text = h.advice
          )
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
    h
  }

  // ---------- 2. 定时器计时 ----------
  var elapsedSeconds by remember { mutableIntStateOf(0) }

  LaunchedEffect(Unit) {
    while (true) {
      delay(1000)
      elapsedSeconds += 1
    }
  }

  fun getElapsedText(): String {
    return when {
      elapsedSeconds < 60 -> "刚刚"
      elapsedSeconds < 3600 -> "${elapsedSeconds / 60}分钟前"
      else -> "${elapsedSeconds / 3600}小时前"
    }
  }

  // ---------- 3. UI 调色板与数值转换 ----------
  val primaryColor = if (isDarkMode) Color(0xFF58B582) else Color(0xFF1E5F41)
  val cardBgColor = if (isDarkMode) Color(0xFF1E2226) else Color.White
  val subCardBgColor = if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color(0xFFF5F7F6)
  val mainTextColor = if (isDarkMode) Color.White else Color(0xFF222222)
  val subTextColor = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF666666)
  val borderColor = if (isDarkMode) Color(0xFF283038) else Color(0xFFEEEEEE)

  val lightDisplayValue = remember(device.light, lightUnit) {
    if (lightUnit == "PAR") {
      val num = device.light.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
      String.format(Locale.US, "%.1f PAR", num * 0.0185)
    } else {
      "${device.light} LUX"
    }
  }

  // ---------- 4. 界面绘制 ----------
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(cardBgColor, shape = RoundedCornerShape(16.dp))
      .border(1.dp, borderColor, shape = RoundedCornerShape(16.dp))
      .padding(14.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.flower_pot),
          contentDescription = "Device Icon",
          colorFilter = ColorFilter.tint(primaryColor),
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = device.name,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = mainTextColor
        )
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier
            .background(health.badgeBg, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .background(health.color, CircleShape)
          )
          Text(
            text = health.statusText,
            fontSize = 11.sp,
            color = health.color,
            fontWeight = FontWeight.Bold
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          Box(
            modifier = Modifier
              .size(5.dp)
              .background(Color(0xFF4CAF50), CircleShape)
          )
          Text(
            text = "BLE 在线",
            fontSize = 10.sp,
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    Column(
      modifier = Modifier.padding(bottom = 10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        MetricGridItem(
          modifier = Modifier.weight(1f),
          iconRes = R.drawable.humidity,
          title = "土壤湿度",
          value = device.moisture,
          diagLabel = mDiag.label,
          diagColor = mDiag.color,
          primaryColor = primaryColor,
          subCardBgColor = subCardBgColor,
          mainTextColor = mainTextColor,
          subTextColor = subTextColor
        )

        MetricGridItem(
          modifier = Modifier.weight(1f),
          iconRes = R.drawable.temperature,
          title = "土壤温度",
          value = device.temp,
          diagLabel = tDiag.label,
          diagColor = tDiag.color,
          primaryColor = primaryColor,
          subCardBgColor = subCardBgColor,
          mainTextColor = mainTextColor,
          subTextColor = subTextColor
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        MetricGridItem(
          modifier = Modifier.weight(1f),
          iconRes = R.drawable.brightness,
          title = "光照强度",
          value = lightDisplayValue,
          diagLabel = lDiag.label,
          diagColor = lDiag.color,
          primaryColor = primaryColor,
          subCardBgColor = subCardBgColor,
          mainTextColor = mainTextColor,
          subTextColor = subTextColor
        )

        MetricGridItem(
          modifier = Modifier.weight(1f),
          iconRes = R.drawable.nutrition,
          title = "土壤养分",
          value = device.npk,
          diagLabel = nDiag.label,
          diagColor = nDiag.color,
          primaryColor = primaryColor,
          subCardBgColor = subCardBgColor,
          mainTextColor = mainTextColor,
          subTextColor = subTextColor
        )
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          color = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color(0xFFF2F6F3),
          shape = RoundedCornerShape(10.dp)
        )
        .border(
          width = 1.dp,
          color = health.color.copy(alpha = 0.2f),
          shape = RoundedCornerShape(10.dp)
        )
        .padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
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
            painter = painterResource(id = R.drawable.tip),
            contentDescription = "Tip Icon",
            colorFilter = ColorFilter.tint(health.color),
            modifier = Modifier.size(13.dp)
          )
          Text(
            text = "养护诊断通知",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = health.color
          )
        }

        Text(
          text = getElapsedText(),
          fontSize = 10.sp,
          color = if (isDarkMode) Color.White.copy(alpha = 0.4f) else Color(0xFF999999)
        )
      }

      Text(
        text = health.advice,
        fontSize = 11.5.sp,
        color = if (isDarkMode) Color.White.copy(alpha = 0.85f) else Color(0xFF333333),
        lineHeight = 16.sp,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
private fun MetricGridItem(
  modifier: Modifier = Modifier,
  iconRes: Int,
  title: String,
  value: String,
  diagLabel: String,
  diagColor: Color,
  primaryColor: Color,
  subCardBgColor: Color,
  mainTextColor: Color,
  subTextColor: Color
) {
  Column(
    modifier = modifier
      .background(subCardBgColor, shape = RoundedCornerShape(10.dp))
      .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(2.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Image(
        painter = painterResource(id = iconRes),
        contentDescription = title,
        colorFilter = ColorFilter.tint(primaryColor),
        modifier = Modifier.size(14.dp)
      )

      Spacer(modifier = Modifier.width(6.dp))

      Text(
        text = title,
        fontSize = 11.sp,
        color = subTextColor
      )

      Spacer(modifier = Modifier.weight(1f))

      Text(
        text = diagLabel,
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Bold,
        color = diagColor
      )
    }

    Text(
      text = value,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = mainTextColor
    )
  }
}
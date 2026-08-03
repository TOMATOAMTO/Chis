package com.example.chis.components

import com.example.chis.model.SimulatedDeviceNode

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chis.R

@Composable
fun AddDeviceSheet(
  isScanning: Boolean,
  availableDevices: List<SimulatedDeviceNode>,
  isDarkMode: Boolean,
  isEnglish: Boolean,
  onClose: () -> Unit = {},
  onSelectDeviceToBind: (SimulatedDeviceNode) -> Unit = {}
) {
  // 调色板
  val bgColor = if (isDarkMode) Color(0xFF1E2226) else Color.White
  val textColor = if (isDarkMode) Color.White else Color(0xFF222222)
  val cardBgColor = if (isDarkMode) Color(0xFF242C33) else Color(0xFFF5F7F6)
  val primaryGreen = Color(0xFF224A32)

  // 雷达脉冲无限动画
  val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
  val radarScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.4f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "RadarScale"
  )

  // 设备短名称格式化
  fun getShortDeviceName(rawName: String?): String {
    if (rawName.isNullOrEmpty()) return "智能设备"
    if (rawName.contains("SmartPot_01")) return "SmartPot_01"
    if (rawName.contains("PlantSensor_02")) return "PlantSensor_02"
    val parts = rawName.split("_")
    return parts.lastOrNull() ?: rawName
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(bgColor)
      .padding(24.dp)
  ) {
    // ---------- 1. Header Bar ----------
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isEnglish) "Add Smart Device" else "添加智能设备",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = textColor
      )

      Image(
        painter = painterResource(id = R.drawable.left), // ✅ 已替换：app.media.left
        contentDescription = "Close",
        colorFilter = ColorFilter.tint(if (isDarkMode) Color.White else Color(0xFF333333)),
        modifier = Modifier
          .size(20.dp)
          .clickable { onClose() }
      )
    }

    // ---------- 2. 状态分支 ----------
    if (isScanning) {
      // ----- 扫描状态：蓝牙雷达动画 -----
      Column(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = if (isEnglish) "Bluetooth BLE Scanning" else "蓝牙雷达扫描",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = textColor,
          modifier = Modifier.padding(bottom = 44.dp)
        )

        Box(
          modifier = Modifier
            .size(160.dp)
            .padding(top = 4.dp, bottom = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(130.dp)
              .scale(radarScale * 1.2f)
              .background(Color(0x1F224A32), CircleShape)
          )

          Box(
            modifier = Modifier
              .size(90.dp)
              .scale(radarScale)
              .background(Color(0x38224A32), CircleShape)
          )

          Box(
            modifier = Modifier
              .size(54.dp)
              .background(primaryGreen, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.flower_pot), // ✅ 已替换：app.media.flower_pot
              contentDescription = "Flower Pot",
              colorFilter = ColorFilter.tint(Color.White),
              modifier = Modifier
                .size(26.dp)
                .offset(x = 2.dp)
            )
          }
        }

        Text(
          text = if (isEnglish) "Searching for nearby smart pots..." else "正在搜索 10cm 内的智能花盆与检测仪...",
          fontSize = 13.sp,
          color = if (isDarkMode) Color(0xFF8AB399) else Color(0xFF666666),
          modifier = Modifier.padding(top = 48.dp, bottom = 16.dp)
        )
      }
    } else {
      // ----- 列表状态：搜索到的附近硬件 -----
      Column(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f)
      ) {
        Text(
          text = if (isEnglish) "Discovered Nearby Hardware" else "已搜到附近智能设备",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF888888),
          modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(availableDevices) { device: SimulatedDeviceNode ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(cardBgColor, shape = RoundedCornerShape(14.dp))
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .background(primaryGreen, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Image(
                    painter = painterResource(id = R.drawable.flower_pot), // ✅ 已替换：app.media.flower_pot
                    contentDescription = "Pot Icon",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(21.dp)
                  )
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                  Text(
                    text = getShortDeviceName(device.devHardwareName),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                  )

                  Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                      text = device.type,
                      fontSize = 11.sp,
                      color = Color(0xFF888888),
                      fontWeight = FontWeight.Medium
                    )

                    if (device.isBound && device.boundPlantName.isNotEmpty()) {
                      Text(
                        text = "· 已绑定: ${device.boundPlantName}",
                        fontSize = 11.sp,
                        color = primaryGreen,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
              }

              if (!device.isBound) {
                Button(
                  onClick = { onSelectDeviceToBind(device) },
                  modifier = Modifier.height(32.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                  shape = RoundedCornerShape(16.dp)
                ) {
                  Text(
                    text = if (isEnglish) "Bind" else "一键绑定",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
              } else {
                Button(
                  onClick = { },
                  enabled = false,
                  modifier = Modifier.height(32.dp),
                  colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = primaryGreen.copy(alpha = 0.35f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                  ),
                  shape = RoundedCornerShape(16.dp)
                ) {
                  Text(
                    text = if (isEnglish) "Bound" else "已绑定",
                    fontSize = 12.sp
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
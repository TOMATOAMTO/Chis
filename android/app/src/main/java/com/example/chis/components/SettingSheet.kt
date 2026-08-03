package com.example.chis.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.chis.R

@Composable
fun SettingSheet(
  isDarkMode: Boolean,
  isEnglish: Boolean,
  lightUnit: String,
  onClose: () -> Unit = {},
  onEnglishChange: (Boolean) -> Unit = {},
  onLightUnitChange: (String) -> Unit = {}
) {
  val context = LocalContext.current

  // ---------- 调色板 ----------
  val bgColor = if (isDarkMode) Color(0xFF1E2226) else Color.White
  val titleTextColor = if (isDarkMode) Color.White else Color(0xFF222222)
  val itemTextColor = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF333333)
  val activeColor = if (isDarkMode) Color(0xFF34C759) else Color(0xFF224A32)
  val inactiveColor = Color(0xFF888888)
  val dividerColor = if (isDarkMode) Color(0xFF242C33) else Color(0xFFEFEFEF)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .background(bgColor)
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // ---------- 1. Header Bar ----------
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isEnglish) "Settings" else "系统设置",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = titleTextColor
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

    HorizontalDivider(color = dividerColor, thickness = 1.dp)

    // ---------- 2. 应用语言选择 ----------
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isEnglish) "Language" else "应用语言选择",
        fontSize = 14.sp,
        color = itemTextColor
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "中文",
          fontSize = 13.sp,
          fontWeight = if (!isEnglish) FontWeight.Bold else FontWeight.Normal,
          color = if (!isEnglish) activeColor else inactiveColor,
          modifier = Modifier.clickable { onEnglishChange(false) }
        )

        Text(
          text = "|",
          fontSize = 12.sp,
          color = Color(0xFFDDDDDD)
        )

        Text(
          text = "English",
          fontSize = 13.sp,
          fontWeight = if (isEnglish) FontWeight.Bold else FontWeight.Normal,
          color = if (isEnglish) activeColor else inactiveColor,
          modifier = Modifier.clickable { onEnglishChange(true) }
        )
      }
    }

    HorizontalDivider(color = dividerColor, thickness = 1.dp)

    // ---------- 3. 光照单位选择 ----------
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isEnglish) "Light Sensor Unit" else "光照单位选择",
        fontSize = 14.sp,
        color = itemTextColor
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "LUX",
          fontSize = 13.sp,
          fontWeight = if (lightUnit == "LUX") FontWeight.Bold else FontWeight.Normal,
          color = if (lightUnit == "LUX") activeColor else inactiveColor,
          modifier = Modifier.clickable { onLightUnitChange("LUX") }
        )

        Text(
          text = "|",
          fontSize = 12.sp,
          color = Color(0xFFDDDDDD)
        )

        Text(
          text = "PAR",
          fontSize = 13.sp,
          fontWeight = if (lightUnit == "PAR") FontWeight.Bold else FontWeight.Normal,
          color = if (lightUnit == "PAR") activeColor else inactiveColor,
          modifier = Modifier.clickable { onLightUnitChange("PAR") }
        )
      }
    }

    HorizontalDivider(color = dividerColor, thickness = 1.dp)

    // ---------- 4. 检查更新 ----------
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable {
          Toast.makeText(
            context,
            "已是最新版本 (v0.0.1)",
            Toast.LENGTH_LONG
          ).show()
        },
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isEnglish) "Check for Updates" else "检查更新",
        fontSize = 14.sp,
        color = itemTextColor
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "v0.0.1 (已是最新)",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF34C759)
        )

        Image(
          painter = painterResource(id = if (isDarkMode) R.drawable.right_night else R.drawable.right),
          contentDescription = "Arrow Right",
          modifier = Modifier.size(14.dp)
        )
      }
    }

    HorizontalDivider(color = dividerColor, thickness = 1.dp)

    // ---------- 5. 设备固件版本 ----------
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "设备固件版本",
        fontSize = 14.sp,
        color = itemTextColor
      )

      Text(
        text = "v0.0.1",
        fontSize = 13.sp,
        color = inactiveColor
      )
    }
  }
}
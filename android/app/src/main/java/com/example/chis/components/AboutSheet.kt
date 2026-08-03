package com.example.chis.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chis.R

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun AboutSheet(
  isDarkMode: Boolean,
  isEnglish: Boolean,
  onClose: () -> Unit = {}
) {
  // ---------- 调色板 (1:1 忠实还原 ArkTS) ----------
  val bgColor = if (isDarkMode) Color(0xFF1E2226) else Color.White
  val titleTextColor = if (isDarkMode) Color.White else Color(0xFF222222)
  val appTitleColor = if (isDarkMode) Color.White else Color(0xFF1C1C1E)
  val versionTextColor = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8E8E93)
  val dividerColor = if (isDarkMode) Color(0xFF242C33) else Color(0xFFEFEFEF)

  val cardBgColor = if (isDarkMode) Color(0xFF242C33) else Color(0xFFF5F7F6)
  val cardTitleColor = if (isDarkMode) Color.White else Color(0xFF1C1C1E)
  val cardContentColor = if (isDarkMode) Color(0xFFE5E5EA) else Color(0xFF3A3A3C)
  val cardSubTextColor = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8E8E93)
  val cardDividerColor = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
  val iconColor = if (isDarkMode) Color(0xFF58B582) else Color(0xFF224A32)

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
        text = if (isEnglish) "About App" else "关于软件",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = titleTextColor
      )

      Image(
        painter = painterResource(id = R.drawable.left), // app.media.left
        contentDescription = "Close",
        colorFilter = ColorFilter.tint(if (isDarkMode) Color.White else Color(0xFF333333)),
        modifier = Modifier
          .size(20.dp)
          .clickable { onClose() }
      )
    }

    HorizontalDivider(color = dividerColor, thickness = 1.dp)

    // ---------- 2. App Logo & Header Section ----------
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, bottom = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Image(
        painter = painterResource(id = R.drawable.icon), // app.media.icon
        contentDescription = "App Icon",
        modifier = Modifier
          .size(68.dp)
          .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
          .clip(RoundedCornerShape(16.dp))
      )

      Text(
        text = if (isEnglish) "Chis" else "青莳",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = appTitleColor,
        modifier = Modifier.padding(top = 4.dp)
      )

      Text(
        text = "Version 0.0.1 (Build 2026)",
        fontSize = 12.sp,
        color = versionTextColor
      )
    }

    // ---------- 3. Statement Card (Apple Inset Grouped Box) ----------
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(cardBgColor, shape = RoundedCornerShape(14.dp))
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.tip), // app.media.tip
          contentDescription = "Tip Icon",
          colorFilter = ColorFilter.tint(iconColor),
          modifier = Modifier.size(16.dp)
        )

        Text(
          text = "软件开发与使用声明",
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold,
          color = cardTitleColor
        )
      }

      Text(
        text = "此 APP 为刘威爽用于个人学习开发展示，无商业用途。",
        fontSize = 13.sp,
        color = cardContentColor,
        lineHeight = 20.sp,
        modifier = Modifier.fillMaxWidth()
      )

      HorizontalDivider(color = cardDividerColor, thickness = 1.dp)

    }
  }
}
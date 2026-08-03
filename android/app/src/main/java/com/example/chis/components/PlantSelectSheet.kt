package com.example.chis.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chis.R
import com.example.chis.model.PlantRawItem
import com.example.chis.model.SimulatedDeviceNode
import java.util.Locale

@Composable
fun PlantSelectSheet(
  targetBindingDevice: SimulatedDeviceNode?,
  availableDevices: List<SimulatedDeviceNode>,
  plantCatalog: List<PlantRawItem>,
  isDarkMode: Boolean,
  isEnglish: Boolean,
  onClose: () -> Unit = {},
  onDeviceBoundSuccess: (updatedCount: Int, remainingUnbound: Boolean) -> Unit = { _, _ -> }
) {
  val context = LocalContext.current

  // ---------- 搜索状态与过滤逻辑 ----------
  var searchQuery by remember { mutableStateOf("") }

  val filteredPlants = remember(searchQuery, plantCatalog) {
    if (searchQuery.trim().isEmpty()) {
      plantCatalog
    } else {
      val query = searchQuery.trim().lowercase(Locale.ROOT)
      plantCatalog.filter { plant ->
        (plant.name?.lowercase(Locale.ROOT)?.contains(query) == true) ||
                (plant.category?.lowercase(Locale.ROOT)?.contains(query) == true) ||
                (plant.desc?.lowercase(Locale.ROOT)?.contains(query) == true)
      }
    }
  }

  // ---------- 调色板 ----------
  val bgColor = if (isDarkMode) Color(0xFF1E2226) else Color.White
  val headerTextColor = if (isDarkMode) Color.White else Color(0xFF222222)
  val searchBgColor = if (isDarkMode) Color(0xFF242C33) else Color(0xFFF5F7F6)
  val itemBgColor = if (isDarkMode) Color(0xFF242C33) else Color(0xFFF5F7F6)
  val primaryGreen = Color(0xFF224A32)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(bgColor)
      .padding(20.dp)
  ) {
    // ---------- Header Bar ----------
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isEnglish) "Select Plant Species" else "选择关联种植的植物种类",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = headerTextColor
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

    // ---------- 搜索框 ----------
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(38.dp)
        .background(searchBgColor, shape = RoundedCornerShape(19.dp))
        .padding(horizontal = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Image(
        painter = painterResource(id = R.drawable.search), // ✅ 已替换：app.media.search
        contentDescription = "Search Icon",
        colorFilter = ColorFilter.tint(Color(0xFF888888)),
        modifier = Modifier.size(15.dp)
      )

      BasicTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
          fontSize = 13.sp,
          color = if (isDarkMode) Color.White else Color(0xFF222222)
        ),
        decorationBox = { innerTextField ->
          if (searchQuery.isEmpty()) {
            Text(
              text = if (isEnglish) "Search plant name..." else "搜索植物名称、科属...",
              fontSize = 13.sp,
              color = Color(0xFF999999)
            )
          }
          innerTextField()
        },
        modifier = Modifier.weight(1f)
      )

      if (searchQuery.isNotEmpty()) {
        Text(
          text = "✕",
          fontSize = 12.sp,
          color = Color(0xFF888888),
          modifier = Modifier
            .clickable { searchQuery = "" }
            .padding(4.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // ---------- 植物列表 ----------
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(filteredPlants) { plant ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(itemBgColor, shape = RoundedCornerShape(12.dp))
            .clickable {
              if (targetBindingDevice != null) {
                val targetId = targetBindingDevice.id
                val plantName = plant.name ?: ""

                // 找到目标设备并更新可变字段
                availableDevices.find { it.id == targetId }?.let { dev ->
                  dev.isBound = true
                  dev.boundPlantName = plantName
                  dev.name = plantName
                }

                val updatedCount = availableDevices.count { it.isBound }
                val remainingUnbound = availableDevices.any { !it.isBound }

                Toast.makeText(
                  context,
                  "已成功绑定：$plantName",
                  Toast.LENGTH_LONG
                ).show()

                onDeviceBoundSuccess(updatedCount, remainingUnbound)
              }
            }
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(primaryGreen, shape = CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.flower_pot), // ✅ 已替换：app.media.flower_pot
              contentDescription = "Flower Pot",
              colorFilter = ColorFilter.tint(Color.White),
              modifier = Modifier
                .size(20.dp)
                .offset(x = 1.dp)
            )
          }

          Column(
            modifier = Modifier
              .padding(start = 10.dp)
              .weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = plant.name ?: "",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color(0xFF222222)
              )

              Box(
                modifier = Modifier
                  .background(
                    color = Color(0x1F224A32),
                    shape = RoundedCornerShape(6.dp)
                  )
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = plant.category ?: "",
                  fontSize = 10.sp,
                  color = primaryGreen
                )
              }
            }

            Text(
              text = plant.desc ?: "",
              fontSize = 11.sp,
              color = Color(0xFF888888),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Box(
            modifier = Modifier
              .background(primaryGreen, shape = RoundedCornerShape(14.dp))
              .padding(horizontal = 14.dp, vertical = 5.dp)
          ) {
            Text(
              text = "绑定",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }
    }
  }
}
package com.example.chis.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chis.R
import com.example.chis.model.CityNode

@Composable
fun CitySelectSheet(
  isCitySheetOpen: Boolean = true,
  currentCityName: String,
  currentCityLocation: String,
  chinaCities: List<CityNode>,
  isDarkMode: Boolean,
  isEnglish: Boolean,
  onClose: () -> Unit = {},
  onCitySelected: (name: String, location: String) -> Unit = { _, _ -> }
) {
  val bgColor = if (isDarkMode) Color(0xFF1E2226) else Color.White
  val titleTextColor = if (isDarkMode) Color.White else Color(0xFF222222)
  val itemBgColor = if (isDarkMode) Color(0xFF283038) else Color(0xFFF5F7F6)
  val selectedBgColor = Color(0xFF1E5F41)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(bgColor)
      .padding(20.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isEnglish) "Select City Location" else "选择气象定位城市",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = titleTextColor
      )

      Image(
        painter = painterResource(id = R.drawable.left),
        contentDescription = "Close",
        colorFilter = ColorFilter.tint(if (isDarkMode) Color.White else Color(0xFF333333)),
        modifier = Modifier
          .size(20.dp)
          .clickable { onClose() }
      )
    }

    LazyVerticalGrid(
      columns = GridCells.Fixed(3),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(chinaCities) { city ->
        val isSelected = (currentCityName == city.name) || (currentCityLocation == city.location)

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              if (isSelected) selectedBgColor else itemBgColor,
              shape = RoundedCornerShape(10.dp)
            )
            .clickable {
              onCitySelected(city.name, city.location)
            }
            .padding(vertical = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = city.name,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else (if (isDarkMode) Color.White else Color(0xFF333333))
          )
        }
      }
    }
  }
}
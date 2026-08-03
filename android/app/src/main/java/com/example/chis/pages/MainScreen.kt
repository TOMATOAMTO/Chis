package com.example.chis.pages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.chis.R
import com.example.chis.components.*
import com.example.chis.model.CityNode
import com.example.chis.model.PlantApiItem
import com.example.chis.model.PlantRawItem
import com.example.chis.model.SimulatedDeviceNode
import com.example.chis.services.HuaweiCloudService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.Locale

private data class TabItem(
  val title: String,
  val enTitle: String,
  val iconRes: Int,
  val pad: Float = 1.5f,
  val iconSize: Int = 22,
  val offsetY: Int = 0
)

enum class DiagnosticStatus { LOW, NORMAL, HIGH }

data class MetricDiagnostic(
  val status: DiagnosticStatus,
  val label: String,
  val text: String,
  val colorHex: String
)

data class OverallHealth(
  val statusText: String,
  val advice: String,
  val colorHex: String,
  val badgeBgHex: String
)

@Composable
fun MainScreen(
  onLogOut: () -> Unit = {}
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { }

  LaunchedEffect(Unit) {
    val perms = mutableListOf(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      perms.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    permissionLauncher.launch(perms.toTypedArray())
  }

  var currentTabIndex by remember { mutableIntStateOf(0) }
  var reportPeriod by remember { mutableStateOf("Monthly") }
  var isSettingSheetOpen by remember { mutableStateOf(false) }
  var isDarkMode by remember { mutableStateOf(false) }
  var isEnglish by remember { mutableStateOf(false) }
  var isAboutSheetOpen by remember { mutableStateOf(false) }
  var lightUnit by remember { mutableStateOf("LUX") }

  var hwToken by remember { mutableStateOf("") }
  var currentCityName by remember { mutableStateOf("定位中...") }
  var currentCityLocation by remember { mutableStateOf("auto") }
  var isCitySheetOpen by remember { mutableStateOf(false) }

  var isCustomDetailOpen by remember { mutableStateOf(false) }
  var selectedPlant by remember { mutableStateOf<PlantApiItem?>(null) }

  var isAddDeviceSheetOpen by remember { mutableStateOf(false) }
  var isScanning by remember { mutableStateOf(true) }
  var boundDevicesCount by remember { mutableIntStateOf(0) }

  var isSelectPlantSheetOpen by remember { mutableStateOf(false) }
  var targetBindingDevice by remember { mutableStateOf<SimulatedDeviceNode?>(null) }
  var showLogoutDialog by remember { mutableStateOf(false) }

  val chinaCities = remember {
    mutableStateListOf(
      CityNode("自动定位", "auto"),
      CityNode("北京", "116.41,39.92"),
      CityNode("上海", "121.47,31.23"),
      CityNode("广州", "113.26,23.13"),
      CityNode("深圳", "114.05,22.54"),
      CityNode("香港", "114.17,22.28"),
      CityNode("澳门", "113.54,22.19"),
      CityNode("杭州", "120.15,30.28"),
      CityNode("成都", "104.06,30.67"),
      CityNode("武汉", "114.30,30.59"),
      CityNode("南京", "118.79,32.06"),
      CityNode("西安", "108.94,34.34")
    )
  }

  val plantCatalog = remember { mutableStateListOf<PlantRawItem>() }

  val availableDevices = remember {
    mutableStateListOf(
      SimulatedDeviceNode(
        id = "BLE-POT-01",
        devHardwareName = "6a5bb330cbb0cf6bb9707167_SmartPot_01",
        name = "SmartPot_01",
        type = "智能花盆",
        rssi = "-42 dBm",
        isBound = false,
        boundPlantName = "",
        moisture = "60%",
        temp = "24.8°C",
        light = "3200",
        npk = "880 NPK"
      ),
      SimulatedDeviceNode(
        id = "BLE-DET-02",
        devHardwareName = "6a5bb330cbb0cf6bb9707167_PlantSensor_02",
        name = "PlantSensor_02",
        type = "智能花草检测仪",
        rssi = "-55 dBm",
        isBound = false,
        boundPlantName = "",
        moisture = "42%",
        temp = "26.0°C",
        light = "4500",
        npk = "620 NPK"
      )
    )
  }

  val tabItems = remember {
    listOf(
      TabItem("主页", "Home", R.drawable.home, 1.5f, 22, 0),
      TabItem("数据", "Data", R.drawable.trend, 1.5f, 22, 0),
      TabItem("植物志", "Flora", R.drawable.leaves, 1.5f, 26, 1),
      TabItem("我的", "Profile", R.drawable.profile, 1.5f, 22, 0)
    )
  }

  LaunchedEffect(Unit) {
    scope.launch(Dispatchers.IO) {
      try {
        val jsonStr = context.assets.open("plants.json").bufferedReader().use { it.readText() }
        val jsonArr = JSONArray(jsonStr)
        val pList = mutableListOf<PlantRawItem>()
        for (i in 0 until jsonArr.length()) {
          val obj = jsonArr.getJSONObject(i)
          pList.add(PlantRawItem(obj.optString("id"), obj.optString("name"), obj.optString("category"), obj.optString("desc")))
        }
        withContext(Dispatchers.Main) {
          plantCatalog.clear()
          plantCatalog.addAll(pList)
        }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          plantCatalog.clear()
          plantCatalog.addAll(
            listOf(
              PlantRawItem("p1", "薄荷", "香草类", "喜光湿润"),
              PlantRawItem("p2", "多肉植物", "景天科", "耐旱喜光"),
              PlantRawItem("p3", "绿萝", "天南星科", "极易耐阴"),
              PlantRawItem("p4", "龟背竹", "观叶植物", "喜温湿润")
            )
          )
        }
      }

      try {
        val jsonStr = context.assets.open("cities.json").bufferedReader().use { it.readText() }
        val jsonArr = JSONArray(jsonStr)
        val cList = mutableListOf<CityNode>()
        for (i in 0 until jsonArr.length()) {
          val obj = jsonArr.getJSONObject(i)
          cList.add(CityNode(name = obj.optString("name"), location = obj.optString("location")))
        }
        withContext(Dispatchers.Main) {
          chinaCities.clear()
          chinaCities.addAll(cList)
        }
      } catch (_: Exception) {}
    }
  }

  fun startRealLocationFetch() {
    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    if (hasFine || hasCoarse) {
      scope.launch(Dispatchers.IO) {
        try {
          val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
          val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

          if (location != null) {
            val geocoder = Geocoder(context, Locale.CHINA)
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
              val city = addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea
              if (city != null) {
                val cleanCityName = city.replace("市", "").replace("区", "").replace("县", "")
                withContext(Dispatchers.Main) {
                  currentCityName = cleanCityName
                  currentCityLocation = "${location.longitude},${location.latitude}"
                }
                return@launch
              }
            }
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  LaunchedEffect(currentCityLocation) {
    if (currentCityLocation == "auto") {
      startRealLocationFetch()
    }
  }

  fun fetchAllDeviceShadows() {
    scope.launch {
      HuaweiCloudService.fetchAllDeviceShadows(hwToken, availableDevices)
    }
  }

  LaunchedEffect(Unit) {
    val token = HuaweiCloudService.fetchHwToken()
    if (token.isNotEmpty()) {
      hwToken = token
      fetchAllDeviceShadows()
    }

    while (true) {
      delay(3000)
      if (hwToken.isNotEmpty()) {
        fetchAllDeviceShadows()
      }
    }
  }

  LaunchedEffect(availableDevices.map { it.isBound }) {
    boundDevicesCount = availableDevices.count { it.isBound }
  }

  fun startBleScanning() {
    fetchAllDeviceShadows()
    isScanning = true
    isAddDeviceSheetOpen = true
    scope.launch {
      delay(5000)
      isScanning = false
    }
  }

  fun closeAllSheets() {
    isCitySheetOpen = false
    isAddDeviceSheetOpen = false
    isSelectPlantSheetOpen = false
    isSettingSheetOpen = false
    isAboutSheetOpen = false
  }

  val mainBgColor = if (isDarkMode) Color(0xFF12161A) else Color(0xFFF4F6F4)
  val bottomBarBg = if (isDarkMode) Color(0xFF1E2226) else Color.White
  val bottomBarBorder = if (isDarkMode) Color(0xFF242C33) else Color(0xFFEEEEEE)
  val activeColor = if (isDarkMode) Color(0xFF58B582) else Color(0xFF1E5F41)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(mainBgColor)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 56.dp)
    ) {
      when (currentTabIndex) {
        0 -> HomeTabContent(
          isDarkMode = isDarkMode,
          isEnglish = isEnglish,
          boundDevicesCount = boundDevicesCount,
          currentCityName = currentCityName,
          currentCityLocation = currentCityLocation,
          lightUnit = lightUnit,
          availableDevices = availableDevices,
          onStartBleScanning = { startBleScanning() },
          onOpenCitySheet = { isCitySheetOpen = true },
          onCityNameChange = { currentCityName = it }
        )
        1 -> TrendsTab(
          isDarkMode = isDarkMode,
          isEnglish = isEnglish,
          availableDevices = availableDevices
        )
        2 -> PlantEncyclopedia(
          isDarkMode = isDarkMode,
          isEnglish = isEnglish,
          onSelectPlant = { plant ->
            selectedPlant = plant
            isCustomDetailOpen = true
          }
        )
        3 -> ProfileTabContent(
          isDarkMode = isDarkMode,
          isEnglish = isEnglish,
          onDarkModeChange = { isDarkMode = it },
          onOpenSettingSheet = { isSettingSheetOpen = true },
          onOpenAboutSheet = { isAboutSheetOpen = true },
          onShowLogoutDialog = { showLogoutDialog = true }
        )
      }
    }

    // 1:1 底部导航栏
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .height(56.dp)
        .background(bottomBarBg)
        .border(width = 0.5.dp, color = bottomBarBorder)
    ) {
      Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        tabItems.forEachIndexed { index, item ->
          val isSelected = currentTabIndex == index

          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .clickable { currentTabIndex = index },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(26.dp)
                .offset(y = item.offsetY.dp),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.title,
                tint = if (isSelected) activeColor else Color(0xFF888888),
                modifier = Modifier.size(item.iconSize.dp)
              )
            }

            Text(
              text = if (isEnglish) item.enTitle else item.title,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) activeColor else Color(0xFF888888)
            )
          }
        }
      }
    }

    // 植物详情弹窗 Panel
    if (isCustomDetailOpen && selectedPlant != null) {
      CustomDetailPanel(
        selectedPlant = selectedPlant!!,
        isDarkMode = isDarkMode,
        isEnglish = isEnglish,
        onClose = { isCustomDetailOpen = false }
      )
    }

    // Modal Sheet 集合
    val anySheetOpen = isCitySheetOpen || isAddDeviceSheetOpen || isSelectPlantSheetOpen || isSettingSheetOpen || isAboutSheetOpen
    if (anySheetOpen) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.45f))
          .clickable { closeAllSheets() }
      ) {
        Column(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .fillMaxHeight(if (isSelectPlantSheetOpen) 0.78f else 0.56f)
            .background(
              if (isDarkMode) Color(0xFF1E2226) else Color.White,
              RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .clickable(enabled = false) {}
        ) {
          Box(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier.size(width = 36.dp, height = 4.dp).background(
                if (isDarkMode) Color(0xFF3A444C) else Color(0xFFE0E0E0),
                CircleShape
              )
            )
          }

          when {
            isCitySheetOpen -> CitySelectSheet(
              isCitySheetOpen = isCitySheetOpen,
              currentCityName = currentCityName,
              currentCityLocation = currentCityLocation,
              chinaCities = chinaCities,
              isDarkMode = isDarkMode,
              isEnglish = isEnglish,
              onClose = { isCitySheetOpen = false },
              onCitySelected = { name, loc ->
                currentCityName = name
                currentCityLocation = loc
                isCitySheetOpen = false
              }
            )
            isAddDeviceSheetOpen -> AddDeviceSheet(
              isScanning = isScanning,
              availableDevices = availableDevices,
              isDarkMode = isDarkMode,
              isEnglish = isEnglish,
              onClose = { isAddDeviceSheetOpen = false },
              onSelectDeviceToBind = { dev ->
                targetBindingDevice = dev
                isAddDeviceSheetOpen = false
                isSelectPlantSheetOpen = true
              }
            )
            isSelectPlantSheetOpen -> PlantSelectSheet(
              targetBindingDevice = targetBindingDevice,
              availableDevices = availableDevices,
              plantCatalog = plantCatalog,
              isDarkMode = isDarkMode,
              isEnglish = isEnglish,
              onClose = { isSelectPlantSheetOpen = false },
              onDeviceBoundSuccess = { _, remaining ->
                isSelectPlantSheetOpen = false
                if (remaining) { isAddDeviceSheetOpen = true }
              }
            )
            isSettingSheetOpen -> SettingSheet(
              isDarkMode = isDarkMode,
              isEnglish = isEnglish,
              lightUnit = lightUnit,
              onClose = { isSettingSheetOpen = false },
              onEnglishChange = { isEnglish = it },
              onLightUnitChange = { lightUnit = it }
            )
            isAboutSheetOpen -> AboutSheet(
              isDarkMode = isDarkMode,
              isEnglish = isEnglish,
              onClose = { isAboutSheetOpen = false }
            )
          }
        }
      }
    }

    if (showLogoutDialog) {
      AlertDialog(
        onDismissRequest = { showLogoutDialog = false },
        title = { Text(text = "确认退出", fontWeight = FontWeight.Bold) },
        text = { Text(text = "您确定要退出当前的账号吗？") },
        confirmButton = {
          TextButton(onClick = {
            showLogoutDialog = false
            onLogOut()
          }) {
            Text(text = "确定", color = Color(0xFFE15C43), fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showLogoutDialog = false }) {
            Text(text = "取消", color = Color.Gray)
          }
        }
      )
    }
  }
}

@Composable
private fun HomeTabContent(
  isDarkMode: Boolean,
  isEnglish: Boolean,
  boundDevicesCount: Int,
  currentCityName: String,
  currentCityLocation: String,
  lightUnit: String,
  availableDevices: List<SimulatedDeviceNode>,
  onStartBleScanning: () -> Unit,
  onOpenCitySheet: () -> Unit,
  onCityNameChange: (String) -> Unit
) {
  val headerBgColor = if (isDarkMode) Color(0xFF12161A) else Color(0xFF1E5F41)

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(headerBgColor)
        .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(horizontalAlignment = Alignment.Start) {
        Text(
          text = if (isEnglish) "My Plants" else "我的植物",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = if (isEnglish) "$boundDevicesCount Devices connected" else "共 $boundDevicesCount 个智能设备绑定",
          fontSize = 13.sp,
          color = Color.White.copy(alpha = 0.7f),
          modifier = Modifier.padding(top = 4.dp)
        )
      }
      Box(
        modifier = Modifier
          .size(40.dp)
          .background(Color.White.copy(alpha = 0.15f), CircleShape)
          .clickable { onStartBleScanning() },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(id = R.drawable.add),
          contentDescription = "Add",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .height(340.dp)
          .padding(top = 14.dp, bottom = 6.dp)
      ) {
        QWeatherCard(
          currentCityName = currentCityName,
          currentCityLocation = currentCityLocation,
          isDarkMode = isDarkMode,
          isEnglish = isEnglish,
          onCityNameChange = onCityNameChange,
          onOpenCitySheet = onOpenCitySheet
        )
      }

      if (boundDevicesCount == 0) {
        Column(
          modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(top = 28.dp, bottom = 32.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            painter = painterResource(id = R.drawable.flower_pot),
            contentDescription = "App Logo",
            tint = Color(0xFF1E5F41),
            modifier = Modifier.size(60.dp).padding(bottom = 12.dp)
          )
          Text(
            text = if (isEnglish) "No Smart Pot Bound" else "尚未绑定智能设备",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF333333),
            modifier = Modifier.padding(bottom = 4.dp)
          )
          Text(
            text = if (isEnglish) "Connect smart pots to enable automated plant care." else "当前环境适合植物生长，绑定智能设备即可开启数据监测。",
            fontSize = 13.sp,
            color = if (isDarkMode) Color(0xFF8AB399) else Color(0xFF777777),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
          )
          Button(
            onClick = onStartBleScanning,
            modifier = Modifier.width(180.dp).height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5F41)),
            shape = RoundedCornerShape(20.dp)
          ) {
            Text(
              text = if (isEnglish) "Add Smart Device" else "立即添加智能设备",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(top = 16.dp, bottom = 20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = if (isEnglish) "BOUND SMART DEVICES" else "已绑定智能设备监测",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color(0xFF8AB399) else Color(0xFF5C7A69),
            modifier = Modifier.padding(horizontal = 4.dp)
          )

          availableDevices.filter { it.isBound }.forEach { device ->
            DeviceCard(
              device = device,
              isDarkMode = isDarkMode,
              lightUnit = lightUnit
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ProfileTabContent(
  isDarkMode: Boolean,
  isEnglish: Boolean,
  onDarkModeChange: (Boolean) -> Unit,
  onOpenSettingSheet: () -> Unit,
  onOpenAboutSheet: () -> Unit,
  onShowLogoutDialog: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(if (isDarkMode) Color(0xFF12161A) else Color(0xFFF5F7F6))
      .verticalScroll(rememberScrollState())
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(if (isDarkMode) Color(0xFF1A1E22) else Color(0xFF1E5F41))
        .padding(start = 24.dp, end = 24.dp, top = 52.dp, bottom = 24.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(60.dp)
            .border(2.dp, Color.White.copy(alpha = 0.45f), CircleShape)
            .background(Color.White, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.app),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clip(CircleShape)
          )
        }

        Column(
          modifier = Modifier.padding(start = 16.dp),
          horizontalAlignment = Alignment.Start
        ) {
          Text(text = "Hello Liu.", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
          Box(
            modifier = Modifier
              .padding(top = 6.dp)
              .background(
                if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.18f),
                RoundedCornerShape(12.dp)
              )
              .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
              .padding(horizontal = 10.dp, vertical = 3.dp)
          ) {
            Text(
              text = if (isEnglish) "STANDARD ACCOUNT" else "普通账户",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }
    }

    Column(modifier = Modifier.padding(16.dp)) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(if (isDarkMode) Color(0xFF1E2226) else Color.White, RoundedCornerShape(16.dp))
          .padding(horizontal = 16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              painter = painterResource(id = R.drawable.moon),
              contentDescription = "Moon",
              tint = if (isDarkMode) Color.White else Color(0xFF222222),
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = if (isEnglish) "Dark Mode" else "夜间模式",
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium,
              color = if (isDarkMode) Color.White else Color(0xFF222222)
            )
          }

          Switch(
            checked = isDarkMode,
            onCheckedChange = onDarkModeChange,
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = Color(0xFF1E5F41)
            )
          )
        }

        HorizontalDivider(color = if (isDarkMode) Color(0xFF242C33) else Color(0xFFF5F5F5))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenSettingSheet() }
            .padding(vertical = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              painter = painterResource(id = R.drawable.setting),
              contentDescription = "Setting",
              tint = if (isDarkMode) Color.White else Color(0xFF222222),
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = if (isEnglish) "Settings" else "系统设置",
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium,
              color = if (isDarkMode) Color.White else Color(0xFF222222)
            )
          }
          Icon(
            painter = painterResource(id = if (isDarkMode) R.drawable.right_night else R.drawable.right),
            contentDescription = "Arrow Right",
            tint = if (isDarkMode) Color.White else Color(0xFF888888),
            modifier = Modifier.size(16.dp)
          )
        }

        HorizontalDivider(color = if (isDarkMode) Color(0xFF242C33) else Color(0xFFF5F5F5))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenAboutSheet() }
            .padding(vertical = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              painter = painterResource(id = R.drawable.about),
              contentDescription = "About",
              tint = if (isDarkMode) Color.White else Color(0xFF222222),
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = if (isEnglish) "About App" else "关于软件",
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium,
              color = if (isDarkMode) Color.White else Color(0xFF222222)
            )
          }
          Icon(
            painter = painterResource(id = if (isDarkMode) R.drawable.right_night else R.drawable.right),
            contentDescription = "Arrow Right",
            tint = if (isDarkMode) Color.White else Color(0xFF888888),
            modifier = Modifier.size(16.dp)
          )
        }

        HorizontalDivider(color = if (isDarkMode) Color(0xFF242C33) else Color(0xFFF5F5F5))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onShowLogoutDialog() }
            .padding(vertical = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              painter = painterResource(id = R.drawable.logout),
              contentDescription = "Logout",
              tint = Color(0xFFE15C43),
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = if (isEnglish) "Log Out" else "退出账号",
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFFE15C43)
            )
          }
        }
      }
    }
  }
}


@Composable
private fun CustomDetailPanel(
  selectedPlant: PlantApiItem,
  isDarkMode: Boolean,
  isEnglish: Boolean,
  onClose: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val gson = remember { com.google.gson.Gson() }

  var detailData by remember(selectedPlant.id) {
    mutableStateOf(com.example.chis.model.PlantDataCache.detailCache[selectedPlant.id])
  }
  var isLoadingDetail by remember(selectedPlant.id) {
    mutableStateOf(detailData == null)
  }

  // 🚀 优先读取 0ms 内存缓存，未命中时在后台发起网络请求
  LaunchedEffect(selectedPlant.id) {
    if (com.example.chis.model.PlantDataCache.detailCache.containsKey(selectedPlant.id)) {
      detailData = com.example.chis.model.PlantDataCache.detailCache[selectedPlant.id]
      isLoadingDetail = false
      return@LaunchedEffect
    }

    scope.launch(Dispatchers.IO) {
      isLoadingDetail = true
      val apiKey = "sk-n9rO6a5a4027caa3c18826"
      val urlStr = "https://perenual.com/api/species/details/${selectedPlant.id}?key=$apiKey"
      try {
        val conn = (java.net.URL(urlStr).openConnection() as java.net.HttpURLConnection).apply {
          requestMethod = "GET"
          readTimeout = 4000
          connectTimeout = 4000
          setRequestProperty("Accept-Encoding", "gzip")
        }
        val rawStream = if (conn.responseCode == 200) conn.inputStream else conn.errorStream
        if (rawStream != null) {
          val stream = if ("gzip".equals(conn.contentEncoding, ignoreCase = true)) {
            java.util.zip.GZIPInputStream(rawStream)
          } else {
            rawStream
          }
          val resStr = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
          conn.disconnect()
          if (resStr.isNotEmpty()) {
            val resObj = gson.fromJson(resStr, com.example.chis.model.PlantDetailResponse::class.java)
            withContext(Dispatchers.Main) {
              detailData = resObj
              com.example.chis.model.PlantDataCache.detailCache[selectedPlant.id] = resObj
            }
          }
        } else {
          conn.disconnect()
        }
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        withContext(Dispatchers.Main) {
          isLoadingDetail = false
        }
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.45f))
      .clickable { onClose() }
  ) {
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .fillMaxHeight(0.85f)
        .background(
          if (isDarkMode) Color(0xFF1E2226) else Color.White,
          RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        )
        .clickable(enabled = false) {}
    ) {
      Box(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier.size(width = 36.dp, height = 4.dp).background(
            if (isDarkMode) Color(0xFF3A444C) else Color(0xFFE0E0E0),
            CircleShape
          )
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(horizontalAlignment = Alignment.Start) {
          Text(
            text = getPlantTransName(selectedPlant, isEnglish),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color.White else Color(0xFF222222)
          )
          Text(
            text = getPlantSubtitleName(selectedPlant),
            fontSize = 13.sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = Color(0xFF888888)
          )
        }

        Icon(
          painter = painterResource(id = R.drawable.left),
          contentDescription = "Close",
          tint = if (isDarkMode) Color.White else Color(0xFF222222),
          modifier = Modifier
            .size(20.dp)
            .clickable { onClose() }
        )
      }

      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // 🎯【植物图片卡片】：高清自适应画幅
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDarkMode) Color(0xFF242C33) else Color(0xFFF5F7F6))
        ) {
          val imgModel = remember(selectedPlant) {
            getPlantImageUrl(selectedPlant)
          }
          val request = remember(imgModel) {
            coil.request.ImageRequest.Builder(context)
              .data(imgModel)
              .setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
              .crossfade(true)
              .placeholder(R.drawable.leaves)
              .error(R.drawable.leaves)
              .build()
          }
          AsyncImage(
            model = request,
            contentDescription = selectedPlant.common_name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }

        // 真实特征面板
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              if (isDarkMode) Color(0xFF242C33) else Color(0xFFF5F7F6),
              RoundedCornerShape(12.dp)
            )
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isEnglish) "Botanical Features" else "真实植物特征信息",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = if (isDarkMode) Color(0xFF58B582) else Color(0xFF1E5F41)
            )
            if (isLoadingDetail) {
              CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = if (isDarkMode) Color(0xFF58B582) else Color(0xFF1E5F41),
                strokeWidth = 2.dp
              )
            }
          }

          DetailRowItem(
            label = "中文常用名",
            value = getPlantTransName(selectedPlant, isEnglish),
            isDarkMode = isDarkMode
          )

          DetailRowItem(
            label = "英文常用名",
            value = detailData?.common_name ?: (selectedPlant.common_name ?: "Unknown"),
            isDarkMode = isDarkMode
          )

          DetailRowItem(
            label = "植物科属",
            value = detailData?.family ?: "常绿植物科",
            isDarkMode = isDarkMode
          )

          DetailRowItem(
            label = "生长周期",
            value = detailData?.cycle ?: (selectedPlant.cycle ?: "多年生 (Perennial)"),
            isDarkMode = isDarkMode
          )

          DetailRowItem(
            label = "浇水需求",
            value = detailData?.watering ?: (selectedPlant.watering ?: "中等水量 (Average)"),
            isDarkMode = isDarkMode
          )

          DetailRowItem(
            label = "光照条件",
            value = detailData?.sunlight?.joinToString(", ")
              ?: (selectedPlant.sunlight?.joinToString(", ") ?: "散光全日照 (Full Sun / Part Shade)"),
            isDarkMode = isDarkMode
          )

          DetailRowItem(
            label = "养护难度",
            value = detailData?.care_level ?: "容易 (Easy / Medium)",
            isDarkMode = isDarkMode
          )

          DetailRowItem(
            label = "室内适应",
            value = if (detailData?.indoor == true) "适合室内栽培" else "适宜室外环境",
            isDarkMode = isDarkMode,
            isLast = true
          )
        }
      }
    }
  }
}

@Composable
private fun DetailRowItem(
  label: String,
  value: String,
  isDarkMode: Boolean,
  isLast: Boolean = false
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 11.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = label,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = if (isDarkMode) Color(0xFF58B582) else Color(0xFF1E5F41),
        modifier = Modifier.width(76.dp)
      )
      Text(
        text = value,
        fontSize = 14.sp,
        color = if (isDarkMode) Color(0xFFE0E5E8) else Color(0xFF333333),
        modifier = Modifier.weight(1f)
      )
    }
    if (!isLast) {
      HorizontalDivider(color = if (isDarkMode) Color(0xFF2D343A) else Color(0xFFEEEEEE))
    }
  }
}

// ====== 1:1 复刻 ArkTS 源码中的 4 大特征计算方法 ======
fun getPlantType(cycle: String?): String {
  return if (cycle?.lowercase()?.contains("perennial") == true) "多年生草本观叶植物" else "室内常绿温带植物"
}

fun getFlowerPeriod(name: String?): String {
  return if (name?.lowercase()?.contains("maple") == true) "无明显花期" else "春末夏初 (5月 - 7月)"
}

fun getPlantSize(name: String?): String {
  return "中小型盆栽"
}

fun getOriginal(name: String?): String {
  return "温带森林/热带雨林区"
}

// ====== 1:1 复刻 ArkTS 源码中的 4 大指标诊断算法与 OverallHealth =====
fun getMoistureDiagnostic(valStr: String): MetricDiagnostic {
  val num = valStr.replace("%", "").toFloatOrNull()
    ?: return MetricDiagnostic(DiagnosticStatus.NORMAL, "水分适宜", "🌱 土壤含水量正常，支持根系顺畅呼吸。", "#4CAF50")
  return when {
    num < 30 -> MetricDiagnostic(DiagnosticStatus.LOW, "干旱缺水", "💧 土壤偏干(＜30%)，叶片易打蔫卷曲，建议沿盆边透浇一次水分。", "#E65100")
    num > 75 -> MetricDiagnostic(DiagnosticStatus.HIGH, "积水过涝", "🚨 土壤极度湿涝(＞75%)，易导致根系缺氧腐烂，建议加强通风并暂停浇水。", "#D32F2F")
    else -> MetricDiagnostic(DiagnosticStatus.NORMAL, "水分适宜", "🌱 土壤湿度维持在最佳 30%~75% 区间，根系生长极佳。", "#4CAF50")
  }
}

fun getTempDiagnostic(valStr: String): MetricDiagnostic {
  val num = valStr.replace("°C", "").toFloatOrNull()
    ?: return MetricDiagnostic(DiagnosticStatus.NORMAL, "温度适宜", "☀️ 环境温度舒适。", "#4CAF50")
  return when {
    num < 15 -> MetricDiagnostic(DiagnosticStatus.LOW, "低温偏冷", "❄️ 气温偏低(＜15°C)生长变缓，建议移入室内温暖避风处越冬。", "#1976D2")
    num > 28 -> MetricDiagnostic(DiagnosticStatus.HIGH, "高温闷热", "🔥 环境温度较高(＞28°C)蒸发加剧，建议遮阴防晒并向周围喷雾降温。", "#D32F2F")
    else -> MetricDiagnostic(DiagnosticStatus.NORMAL, "温度舒适", "☀️ 15°C~28°C 舒适温域，有利于养分积累与新芽抽发。", "#4CAF50")
  }
}

fun getLightDiagnostic(valStr: String): MetricDiagnostic {
  val num = valStr.replace("[^0-9.]".toRegex(), "").toFloatOrNull()
    ?: return MetricDiagnostic(DiagnosticStatus.NORMAL, "光照充足", "✨ 散射光充沛，进行高效光合作用。", "#4CAF50")
  return when {
    num < 1500 -> MetricDiagnostic(DiagnosticStatus.LOW, "光照不足", "☁️ 环境较暗(＜1500 LUX)，长期易导致徒长黄叶，建议移至明亮阳台。", "#E65100")
    num > 6000 -> MetricDiagnostic(DiagnosticStatus.HIGH, "强光灼伤", "☀️ 阳光过强(＞6000 LUX)，可能晒伤娇嫩叶片，请适当拉帘遮阴。", "#D32F2F")
    else -> MetricDiagnostic(DiagnosticStatus.NORMAL, "光效极佳", "✨ 光照强度处于理想散射光区，叶色浓绿光亮。", "#4CAF50")
  }
}

fun getNpkDiagnostic(valStr: String): MetricDiagnostic {
  val num = valStr.replace("[^0-9.]".toRegex(), "").toFloatOrNull()
    ?: return MetricDiagnostic(DiagnosticStatus.NORMAL, "肥力均衡", "🪴 养分充沛，支撑健康生长。", "#4CAF50")
  return when {
    num < 400 -> MetricDiagnostic(DiagnosticStatus.LOW, "养分缺乏", "📉 肥力偏低(＜400 NPK)，植物易黄叶瘦弱，建议适量追施稀释薄肥。", "#E65100")
    num > 1200 -> MetricDiagnostic(DiagnosticStatus.HIGH, "肥害风险", "⚠️ 养分浓度过高(＞1200 NPK)易烧根，建议大水浇透冲洗余肥。", "#D32F2F")
    else -> MetricDiagnostic(DiagnosticStatus.NORMAL, "肥力均衡", "🪴 氮磷钾养分充沛，为植物抽枝发叶提供强劲动力。", "#4CAF50")
  }
}

fun getOverallDeviceHealth(moisture: String, temp: String, light: String, npk: String): OverallHealth {
  val m = getMoistureDiagnostic(moisture)
  val t = getTempDiagnostic(temp)
  val l = getLightDiagnostic(light)
  val n = getNpkDiagnostic(npk)

  if (m.status == DiagnosticStatus.HIGH || t.status == DiagnosticStatus.HIGH || n.status == DiagnosticStatus.HIGH || l.status == DiagnosticStatus.HIGH) {
    val warnText = if (m.status == DiagnosticStatus.HIGH) m.text else if (t.status == DiagnosticStatus.HIGH) t.text else if (n.status == DiagnosticStatus.HIGH) n.text else l.text
    return OverallHealth("注意预警", warnText, "#D32F2F", "rgba(211,47,47,0.1)")
  }
  if (m.status == DiagnosticStatus.LOW || t.status == DiagnosticStatus.LOW || n.status == DiagnosticStatus.LOW || l.status == DiagnosticStatus.LOW) {
    val adviceText = if (m.status == DiagnosticStatus.LOW) m.text else if (t.status == DiagnosticStatus.LOW) t.text else if (n.status == DiagnosticStatus.LOW) n.text else l.text
    return OverallHealth("养护关注", adviceText, "#E65100", "rgba(230,81,0,0.1)")
  }
  return OverallHealth("健康状态良好", "🌟 当前各项指标均处于黄金舒适区间，植物茁壮成长中！", "#224A32", "rgba(34,74,50,0.1)")
}
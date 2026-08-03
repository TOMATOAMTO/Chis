package com.example.chis.services

import androidx.compose.ui.graphics.Color

// TrendsTab 用的枚举
enum class MetricStatus { LOW, NORMAL, HIGH }

// 兼容别名
typealias DiagnosticStatus = MetricStatus

data class MetricDiagnostic(
  val status: MetricStatus,
  val label: String,
  val text: String,
  val color: Color
)

data class OverallHealth(
  val statusText: String,
  val advice: String,
  val color: Color,
  val badgeBg: Color
)

object TelemetryService {
  fun getMoistureDiagnostic(valStr: String): MetricDiagnostic {
    val num = valStr.replace("%", "").toFloatOrNull()
      ?: return MetricDiagnostic(MetricStatus.NORMAL, "水分适宜", "🌱 土壤含水量正常，支持根系顺畅呼吸。", Color(0xFF4CAF50))
    return when {
      num < 30 -> MetricDiagnostic(MetricStatus.LOW, "干旱缺水", "💧 土壤偏干(＜30%)，叶片易打蔫卷曲，建议沿盆边透浇一次水分。", Color(0xFFE65100))
      num > 75 -> MetricDiagnostic(MetricStatus.HIGH, "积水过涝", "🚨 土壤极度湿涝(＞75%)，易导致根系缺氧腐烂，建议加强通风并暂停浇水。", Color(0xFFD32F2F))
      else -> MetricDiagnostic(MetricStatus.NORMAL, "水分适宜", "🌱 土壤湿度维持在最佳 30%~75% 区间，根系生长极佳。", Color(0xFF4CAF50))
    }
  }

  fun getTempDiagnostic(valStr: String): MetricDiagnostic {
    val num = valStr.replace("°C", "").toFloatOrNull()
      ?: return MetricDiagnostic(MetricStatus.NORMAL, "温度适宜", "☀️ 环境温度舒适。", Color(0xFF4CAF50))
    return when {
      num < 15 -> MetricDiagnostic(MetricStatus.LOW, "低温偏冷", "❄️ 气温偏低(＜15°C)生长变缓，建议移入室内温暖避风处越冬。", Color(0xFF1976D2))
      num > 28 -> MetricDiagnostic(MetricStatus.HIGH, "高温闷热", "🔥 环境温度较高(＞28°C)蒸发加剧，建议遮阴防晒并向周围喷雾降温。", Color(0xFFD32F2F))
      else -> MetricDiagnostic(MetricStatus.NORMAL, "温度舒适", "☀️ 15°C~28°C 舒适温域，有利于养分积累与新芽抽发。", Color(0xFF4CAF50))
    }
  }

  fun getLightDiagnostic(valStr: String): MetricDiagnostic {
    val num = valStr.replace("[^0-9.]".toRegex(), "").toFloatOrNull()
      ?: return MetricDiagnostic(MetricStatus.NORMAL, "光照充足", "✨ 散射光充沛，进行高效光合作用。", Color(0xFF4CAF50))
    return when {
      num < 1500 -> MetricDiagnostic(MetricStatus.LOW, "光照不足", "☁️ 环境较暗(＜1500 LUX)，长期易导致徒长黄叶，建议移至明亮阳台。", Color(0xFFE65100))
      num > 6000 -> MetricDiagnostic(MetricStatus.HIGH, "强光灼伤", "☀️ 阳光过强(＞6000 LUX)，可能晒伤娇嫩叶片，请适当拉帘遮阴。", Color(0xFFD32F2F))
      else -> MetricDiagnostic(MetricStatus.NORMAL, "光效极佳", "✨ 光照强度处于理想散射光区，叶色浓绿光亮。", Color(0xFF4CAF50))
    }
  }

  fun getNpkDiagnostic(valStr: String): MetricDiagnostic {
    val num = valStr.replace("[^0-9.]".toRegex(), "").toFloatOrNull()
      ?: return MetricDiagnostic(MetricStatus.NORMAL, "肥力均衡", "🪴 养分充沛，支撑健康生长。", Color(0xFF4CAF50))
    return when {
      num < 400 -> MetricDiagnostic(MetricStatus.LOW, "养分缺乏", "📉 肥力偏低(＜400 NPK)，植物易黄叶瘦弱，建议适量追施稀释薄肥。", Color(0xFFE65100))
      num > 1200 -> MetricDiagnostic(MetricStatus.HIGH, "肥害风险", "⚠️ 养分浓度过高(＞1200 NPK)易烧根，建议大水浇透冲洗余肥。", Color(0xFFD32F2F))
      else -> MetricDiagnostic(MetricStatus.NORMAL, "肥力均衡", "🪴 氮磷钾养分充沛，为植物抽枝发叶提供强劲动力。", Color(0xFF4CAF50))
    }
  }

  fun getOverallDeviceHealth(moisture: String, temp: String, light: String, npk: String): OverallHealth {
    val m = getMoistureDiagnostic(moisture)
    val t = getTempDiagnostic(temp)
    val l = getLightDiagnostic(light)
    val n = getNpkDiagnostic(npk)

    if (m.status == MetricStatus.HIGH || t.status == MetricStatus.HIGH || n.status == MetricStatus.HIGH || l.status == MetricStatus.HIGH) {
      val warnText = if (m.status == MetricStatus.HIGH) m.text else if (t.status == MetricStatus.HIGH) t.text else if (n.status == MetricStatus.HIGH) n.text else l.text
      return OverallHealth("注意预警", warnText, Color(0xFFD32F2F), Color(0x1AD32F2F))
    }
    if (m.status == MetricStatus.LOW || t.status == MetricStatus.LOW || n.status == MetricStatus.LOW || l.status == MetricStatus.LOW) {
      val adviceText = if (m.status == MetricStatus.LOW) m.text else if (t.status == MetricStatus.LOW) t.text else if (n.status == MetricStatus.LOW) n.text else l.text
      return OverallHealth("养护关注", adviceText, Color(0xFFE65100), Color(0x1AE65100))
    }
    return OverallHealth("健康状态良好", "🌟 当前各项指标均处于黄金舒适区间，植物茁壮成长中！", Color(0xFF224A32), Color(0x1A224A32))
  }
}
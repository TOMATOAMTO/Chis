package com.example.chis.model

import com.google.gson.annotations.SerializedName
import java.util.concurrent.ConcurrentHashMap

// ====== 全局极速响应内存缓存池 ======
object PlantDataCache {
    val searchCache = ConcurrentHashMap<String, List<PlantApiItem>>()
    val detailCache = ConcurrentHashMap<Int, PlantDetailResponse>()
}

// ====== Plant API 数据模型 ======
data class PlantImage(
    val thumbnail: String? = null,
    val small_url: String? = null,
    val medium_url: String? = null,
    val regular_url: String? = null,
    val original_url: String? = null
)

data class PlantApiItem(
    val id: Int,
    val common_name: String? = null,
    val scientific_name: List<String>? = null,
    val other_name: List<String>? = null,
    val cycle: String? = null,
    val watering: String? = null,
    val sunlight: List<String>? = null,
    val default_image: PlantImage? = null
)

data class PlantApiResponse(
    val data: List<PlantApiItem>? = null
)

data class PlantDetailResponse(
    val id: Int,
    val common_name: String? = null,
    val scientific_name: List<String>? = null,
    val other_name: List<String>? = null,
    val family: String? = null,
    val origin: List<String>? = null,
    val type: String? = null,
    val dimension: String? = null,
    val cycle: String? = null,
    val watering: String? = null,
    val watering_period: String? = null,
    val sunlight: List<String>? = null,
    val maintenance: String? = null,
    val care_level: String? = null,
    val growth_rate: String? = null,
    val drought_tolerant: Boolean? = null,
    val indoor: Boolean? = null,
    val description: String? = null,
    val default_image: PlantImage? = null
)

data class CityNode(
    val name: String,
    val location: String
)

data class PlantRawItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val desc: String = ""
)

// ====== 搜索引擎 JSON 字典模型 ======
data class GenusNode(
    val cn: String = "",
    val type: String = "",
    val alias: List<String> = emptyList()
)

data class EpithetNode(
    val cn: String = "",
    val position: String = ""
)

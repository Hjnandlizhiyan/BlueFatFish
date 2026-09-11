package com.bigfatfish.release.data.model

import kotlinx.serialization.Serializable

/**
 * 高峰计费时段窗口（来自 DeepSeek 官网解析结果）
 * days：ISO 星期，1=周一 … 7=周日
 * startMinute / endMinute：当天第几分钟；若 endMinute <= startMinute 表示跨天
 */
@Serializable
data class PricingWindow(
    val days: List<Int>,
    val startMinute: Int,
    val endMinute: Int
)

@Serializable
data class PricingItem(
    val label: String,
    val peak: String,
    val idle: String
)

@Serializable
data class PricingModel(
    val model: String,
    val version: String,
    val items: List<PricingItem>
)

/** 从 DeepSeek 官网解析出的定价与时段规则 */
@Serializable
data class RemotePricing(
    val windows: List<PricingWindow>,
    val models: List<PricingModel>,
    val updatedAt: Long
)
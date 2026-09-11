package com.bigfatfish.release.data.remote

import com.bigfatfish.release.data.model.PricingItem
import com.bigfatfish.release.data.model.PricingModel
import com.bigfatfish.release.data.model.PricingWindow
import com.bigfatfish.release.data.model.RemotePricing

/**
 * 解析 DeepSeek 官网「模型 & 价格」页（Docusaurus 渲染的 HTML）。
 *
 * 采用「尽力而为」策略：任一项解析失败即返回 null / 空列表，
 * 由调用方回退到内置数据，避免官网改版时出现错误结果。
 */
object PricingHtmlParser {

    private val priceRowRegex = Regex(
        """<td>(空闲|高峰)时段</td><td>([\d.]+)元</td><td>([\d.]+)元</td>"""
    )
    private val modelRegex = Regex(
        """>模型</td><td>(deepseek-[\w-]+)<sup>.*?</sup></td><td>(deepseek-[\w-]+)<sup>"""
    )
    private val versionRegex = Regex(
        """>模型版本</td><td>([^<]+)</td><td>([^<]+)</td>"""
    )
    private val ruleRegex = Regex(
        """高峰时段为北京时间(.+?)（其余为空闲时段）"""
    )
    private val timeRangeRegex = Regex(
        """(\d{1,2}):(\d{2})\s*[-–~至]\s*(\d{1,2}):(\d{2})"""
    )
    private val dayNameMap = mapOf(
        '一' to 1, '二' to 2, '三' to 3, '四' to 4,
        '五' to 5, '六' to 6, '日' to 7, '天' to 7
    )

    fun parse(html: String): RemotePricing? {
        val windows = parseWindows(html)
        val models = parseModels(html)
        if (windows.isEmpty() || models.isEmpty()) return null
        return RemotePricing(
            windows = windows,
            models = models,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun parseModels(html: String): List<PricingModel> {
        val nameMatch = modelRegex.find(html) ?: return emptyList()
        val name1 = nameMatch.groupValues[1]
        val name2 = nameMatch.groupValues[2]

        val versionMatch = versionRegex.find(html)
        val version1 = versionMatch?.groupValues?.getOrNull(1)?.trim().orEmpty()
        val version2 = versionMatch?.groupValues?.getOrNull(2)?.trim().orEmpty()

        val rows = priceRowRegex.findAll(html).map { match ->
            Triple(match.groupValues[1], match.groupValues[2], match.groupValues[3])
        }.toList()

        // 预期 6 行，顺序：缓存命中(空闲/高峰)、缓存未命中(空闲/高峰)、输出(空闲/高峰)
        if (rows.size < 6) return emptyList()
        val labels = listOf("输入（缓存命中）", "输入（缓存未命中）", "输出")
        val items1 = mutableListOf<PricingItem>()
        val items2 = mutableListOf<PricingItem>()
        for (i in labels.indices) {
            val idleRow = rows[i * 2]
            val peakRow = rows[i * 2 + 1]
            if (idleRow.first != "空闲" || peakRow.first != "高峰") return emptyList()
            items1.add(PricingItem(labels[i], peak = peakRow.third, idle = idleRow.third))
            items2.add(PricingItem(labels[i], peak = peakRow.second, idle = idleRow.second))
        }
        return listOf(
            PricingModel(name1, version1, items1),
            PricingModel(name2, version2, items2)
        )
    }

    private fun parseWindows(html: String): List<PricingWindow> {
        val ruleMatch = ruleRegex.find(html) ?: return emptyList()
        val ruleText = ruleMatch.groupValues[1]
        val windows = mutableListOf<PricingWindow>()
        var currentDays: List<Int>? = null
        for (segment in ruleText.split("、", "，", ",")) {
            val days = parseDays(segment)
            if (days != null) currentDays = days
            val timeMatch = timeRangeRegex.find(segment) ?: continue
            val startMinute = timeMatch.groupValues[1].toInt() * 60 + timeMatch.groupValues[2].toInt()
            val endMinute = timeMatch.groupValues[3].toInt() * 60 + timeMatch.groupValues[4].toInt()
            val daysForSegment = days ?: currentDays ?: (1..7).toList()
            windows.add(PricingWindow(daysForSegment.sorted(), startMinute, endMinute))
        }
        return windows
    }

    private fun parseDays(text: String): List<Int>? {
        if (text.contains("每天") || text.contains("每日") ||
            text.contains("周一至周日") || text.contains("周一至周天")
        ) {
            return (1..7).toList()
        }
        val range = Regex("""周([一二三四五六日天])\s*至\s*周([一二三四五六日天])""").find(text)
        if (range != null) {
            val start = dayNameMap[range.groupValues[1][0]] ?: return null
            val end = dayNameMap[range.groupValues[2][0]] ?: return null
            return if (start <= end) (start..end).toList() else ((start..7) + (1..end)).toList()
        }
        val singles = Regex("""周([一二三四五六日天])""").findAll(text)
            .mapNotNull { dayNameMap[it.groupValues[1][0]] }
            .toList()
        return if (singles.isNotEmpty()) singles else null
    }
}
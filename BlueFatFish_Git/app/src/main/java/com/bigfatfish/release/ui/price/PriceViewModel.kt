package com.bigfatfish.release.ui.price

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bigfatfish.release.data.local.PricingStore
import com.bigfatfish.release.data.local.SettingsStore
import com.bigfatfish.release.data.model.PricingItem
import com.bigfatfish.release.data.model.PricingModel
import com.bigfatfish.release.data.model.PricingWindow
import com.bigfatfish.release.data.remote.DeepSeekPricingApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * DeepSeek 官方「模型 & 价格」默认规则（内置兜底，离线可用）。
 *
 * 来源：https://api-docs.deepseek.com/zh-cn/quick_start/pricing
 * 高峰时段：北京时间周一至周五 09:00-12:00、14:00-18:00；
 * 其余时间（含工作日午休、夜间及周末全天）为空闲时段，空闲价为高峰价的一半。
 *
 * 页面进入时会联网获取官网最新规则并缓存；失败或无网时回退到这里的内置数据。
 */
object DeepSeekPricing {
    const val BEIJING_TIME_ZONE_ID = "Asia/Shanghai"
    const val OFFICIAL_PRICING_URL = DeepSeekPricingApi.PRICING_URL

    /** 内置兜底：高峰时段窗口 */
    val DEFAULT_WINDOWS = listOf(
        PricingWindow(days = (1..5).toList(), startMinute = 9 * 60, endMinute = 12 * 60),
        PricingWindow(days = (1..5).toList(), startMinute = 14 * 60, endMinute = 18 * 60)
    )

    /** 内置兜底：模型与价格（元 / 百万 tokens） */
    val DEFAULT_MODELS = listOf(
        PricingModel(
            model = "deepseek-flash",
            version = "DeepSeek-V4.1-Flash",
            items = listOf(
                PricingItem("输入（缓存命中）", peak = "0.04", idle = "0.02"),
                PricingItem("输入（缓存未命中）", peak = "2", idle = "1"),
                PricingItem("输出", peak = "8", idle = "4")
            )
        ),
        PricingModel(
            model = "deepseek-v4-pro",
            version = "DeepSeek-V4-Pro-0813",
            items = listOf(
                PricingItem("输入（缓存命中）", peak = "0.30", idle = "0.15"),
                PricingItem("输入（缓存未命中）", peak = "9.0", idle = "4.5"),
                PricingItem("输出", peak = "27.0", idle = "13.5")
            )
        )
    )

    fun isoDayOfWeek(calendarDay: Int): Int = when (calendarDay) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        else -> 7
    }

    /** 判断给定北京时间是否落在任一高峰窗口内（支持跨天窗口） */
    fun isPeak(calendar: Calendar, windows: List<PricingWindow>): Boolean {
        val isoDay = isoDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK))
        val prevIsoDay = if (isoDay == 1) 7 else isoDay - 1
        val minuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        for (window in windows) {
            when {
                window.endMinute > window.startMinute -> {
                    if (isoDay in window.days &&
                        minuteOfDay >= window.startMinute &&
                        minuteOfDay < window.endMinute
                    ) return true
                }
                window.endMinute < window.startMinute -> {
                    if (isoDay in window.days && minuteOfDay >= window.startMinute) return true
                    if (prevIsoDay in window.days && minuteOfDay < window.endMinute) return true
                }
            }
        }
        return false
    }
}

data class PriceUiState(
    val beijingClock: String = "--:--:--",
    val phoneClock: String = "--:--:--",
    val phoneZoneName: String = "",
    val beijingDate: String = "",
    val isPeak: Boolean = false,
    val statusTitle: String = "计算中…",
    val statusSubtitle: String = "",
    val countdownText: String = "",
    val nextPeriodName: String = "",
    val segmentProgress: Float = 0f,
    val windows: List<PricingWindow> = DeepSeekPricing.DEFAULT_WINDOWS,
    val models: List<PricingModel> = DeepSeekPricing.DEFAULT_MODELS,
    val dataSourceLabel: String = "内置数据",
    val updatedAtText: String = "",
    val isRefreshing: Boolean = false,
    val notice: String = ""
)

class PriceViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext

    private val _uiState = MutableStateFlow(PriceUiState())
    val uiState: StateFlow<PriceUiState> = _uiState.asStateFlow()

    private val beijingZone: TimeZone = TimeZone.getTimeZone(DeepSeekPricing.BEIJING_TIME_ZONE_ID)
    private val beijingClockFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
        timeZone = beijingZone
    }
    private val beijingDateFormat = SimpleDateFormat("MM月dd日 EEEE", Locale.CHINA).apply {
        timeZone = beijingZone
    }
    private val phoneClockFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val updatedFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    init {
        viewModelScope.launch { loadCacheThenRefresh() }
        viewModelScope.launch {
            while (isActive) {
                _uiState.update { refreshTimePart(it) }
                delay(1000L)
            }
        }
    }

    private suspend fun loadCacheThenRefresh() {
        val cached = withContext(Dispatchers.IO) { PricingStore.load(context) }
        if (cached != null) {
            _uiState.update {
                it.copy(
                    windows = cached.windows,
                    models = cached.models,
                    dataSourceLabel = "官网数据（缓存）",
                    updatedAtText = updatedFormat.format(Date(cached.updatedAt))
                )
            }
        }
        // 复用全局「联网说明」授权，避免未授权时静默联网
        val networkAllowed = withContext(Dispatchers.IO) {
            SettingsStore.hasShownNetworkNotice(context)
        }
        if (networkAllowed) refreshFromOfficial()
    }

    /** 手动 / 进入页面时联网获取官网最新规则；失败则保留当前数据并提示 */
    fun refreshFromOfficial() {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, notice = "") }
            try {
                val pricing = DeepSeekPricingApi.fetch()
                PricingStore.save(context, pricing)
                _uiState.update {
                    it.copy(
                        windows = pricing.windows,
                        models = pricing.models,
                        dataSourceLabel = "官网数据",
                        updatedAtText = updatedFormat.format(Date(pricing.updatedAt)),
                        isRefreshing = false,
                        notice = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        notice = (e.message ?: "官网获取失败") + "，当前显示内置参考数据"
                    )
                }
            }
        }
    }

    private fun refreshTimePart(state: PriceUiState): PriceUiState {
        val now = Date()
        val nowMs = now.time
        val beijingCalendar = Calendar.getInstance(beijingZone).apply { time = now }
        val isPeak = DeepSeekPricing.isPeak(beijingCalendar, state.windows)

        var prevSwitchMs = 0L
        var nextSwitchMs = 0L
        for (boundaryMs in collectBoundaries(nowMs, state.windows)) {
            if (!isSwitchBoundary(boundaryMs, state.windows)) continue
            if (boundaryMs <= nowMs && boundaryMs > prevSwitchMs) prevSwitchMs = boundaryMs
            if (boundaryMs > nowMs && (nextSwitchMs == 0L || boundaryMs < nextSwitchMs)) {
                nextSwitchMs = boundaryMs
            }
        }
        if (nextSwitchMs == 0L) nextSwitchMs = nowMs + 1000L

        val segmentMs = (nextSwitchMs - prevSwitchMs).coerceAtLeast(1L)
        val progress = ((nowMs - prevSwitchMs).toFloat() / segmentMs.toFloat()).coerceIn(0f, 1f)
        val remainingSeconds = ((nextSwitchMs - nowMs) / 1000L).toInt().coerceAtLeast(0)

        return state.copy(
            beijingClock = beijingClockFormat.format(now),
            phoneClock = phoneClockFormat.format(now),
            phoneZoneName = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT),
            beijingDate = beijingDateFormat.format(now),
            isPeak = isPeak,
            statusTitle = if (isPeak) "高峰时段" else "空闲时段",
            statusSubtitle = if (isPeak) "高峰计费 · 标准价格" else "空闲计费 · 价格减半",
            countdownText = formatDuration(remainingSeconds),
            nextPeriodName = if (isPeak) "空闲时段" else "高峰时段",
            segmentProgress = progress
        )
    }

    /** 枚举前后若干天内所有可能的高峰窗口起止时刻 */
    private fun collectBoundaries(nowMs: Long, windows: List<PricingWindow>): List<Long> {
        if (windows.isEmpty()) return emptyList()
        val result = ArrayList<Long>()
        val dayStart = Calendar.getInstance(beijingZone).apply {
            timeInMillis = nowMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        for (offset in -2..8) {
            val dayCalendar = (dayStart.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
            val isoDay = DeepSeekPricing.isoDayOfWeek(dayCalendar.get(Calendar.DAY_OF_WEEK))
            val dayStartMs = dayCalendar.timeInMillis
            for (window in windows) {
                if (isoDay !in window.days) continue
                result.add(dayStartMs + window.startMinute * 60_000L)
                val endDayOffset = if (window.endMinute <= window.startMinute) 1 else 0
                result.add(dayStartMs + (endDayOffset * 1440L + window.endMinute) * 60_000L)
            }
        }
        return result
    }

    private fun isSwitchBoundary(boundaryMs: Long, windows: List<PricingWindow>): Boolean {
        val at = Calendar.getInstance(beijingZone).apply { timeInMillis = boundaryMs }
        val before = Calendar.getInstance(beijingZone).apply { timeInMillis = boundaryMs - 1000L }
        return DeepSeekPricing.isPeak(at, windows) != DeepSeekPricing.isPeak(before, windows)
    }

    private fun formatDuration(totalSeconds: Int): String {
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return when {
            days > 0 -> "$days 天 $hours 小时 $minutes 分"
            hours > 0 -> "$hours 小时 $minutes 分 $seconds 秒"
            else -> "$minutes 分 $seconds 秒"
        }
    }
}

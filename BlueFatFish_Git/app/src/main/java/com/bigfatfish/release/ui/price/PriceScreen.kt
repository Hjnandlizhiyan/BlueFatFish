package com.bigfatfish.release.ui.price

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigfatfish.release.data.model.PricingWindow
import com.bigfatfish.release.ui.theme.LocalAppColors
import com.bigfatfish.release.ui.theme.OrangeBadge
import com.bigfatfish.release.ui.theme.SuccessGreen

private val dayNames = listOf("一", "二", "三", "四", "五", "六", "日")

@Composable
fun PriceScreen(priceViewModel: PriceViewModel, onBack: () -> Unit) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val state by priceViewModel.uiState.collectAsStateWithLifecycle()
    val statusColor = if (state.isPeak) OrangeBadge else SuccessGreen

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.pageBackground)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.tealBtn.copy(alpha = colors.btnBgAlpha),
                    contentColor = colors.tealBtn
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = ButtonDefaults.ContentPadding,
                modifier = Modifier.height(32.dp)
            ) { Text("返回", fontSize = 14.sp) }
            Spacer(Modifier.weight(1f))
            Text("价格一览", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusCard(state, statusColor)
            RuleCard(state)
            PriceTableCard(state)

            if (state.notice.isNotEmpty()) {
                Text(
                    state.notice,
                    fontSize = 12.sp,
                    color = colors.errorTitle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(360.dp)
                )
            }

            Row(
                modifier = Modifier.width(360.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { priceViewModel.refreshFromOfficial() },
                    enabled = !state.isRefreshing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.balancePrimary.copy(alpha = colors.btnBgAlpha),
                        contentColor = colors.balancePrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) { Text(if (state.isRefreshing) "获取中…" else "获取官网数据", fontSize = 14.sp) }
                Button(
                    onClick = { openUrl(context, DeepSeekPricing.OFFICIAL_PRICING_URL) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.indigoBtn.copy(alpha = colors.btnBgAlpha),
                        contentColor = colors.indigoBtn
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) { Text("查看官网", fontSize = 14.sp) }
            }
        }
    }
}

@Composable
private fun StatusCard(state: PriceUiState, statusColor: Color) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .width(360.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            state.statusTitle,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
        Text(state.statusSubtitle, fontSize = 14.sp, color = colors.textSecondary)

        Spacer(Modifier.height(10.dp))
        Text(
            state.beijingClock,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Text("北京时间 · ${state.beijingDate}", fontSize = 12.sp, color = colors.textHint)
        Text("（DeepSeek 计费时区）", fontSize = 12.sp, color = colors.textHint)
        Text(
            "手机时间 ${state.phoneClock} ${state.phoneZoneName}",
            fontSize = 12.sp,
            color = colors.textHint
        )

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.reasoningBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(state.segmentProgress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(3.dp))
                    .background(statusColor)
            )
        }
        Text(
            "距【${state.nextPeriodName}】还有 ${state.countdownText}",
            fontSize = 13.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))
        val sourceText = if (state.updatedAtText.isEmpty()) {
            "数据来源：${state.dataSourceLabel}"
        } else {
            "数据来源：${state.dataSourceLabel} · ${state.updatedAtText} 更新"
        }
        Text(sourceText, fontSize = 11.sp, color = colors.textHint)
    }
}

@Composable
private fun RuleCard(state: PriceUiState) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .width(360.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("收费时段规则", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
        RuleRow(
            "高峰时段",
            formatWindows(state.windows),
            state.isPeak,
            OrangeBadge
        )
        RuleRow(
            "空闲时段",
            "其余时间（含午休、夜间与周末全天）",
            !state.isPeak,
            SuccessGreen
        )
        Text(
            "空闲时段价格为高峰时段价格的一半。",
            fontSize = 12.sp,
            color = colors.textHint
        )
    }
}

@Composable
private fun RuleRow(title: String, desc: String, active: Boolean, color: Color) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) color.copy(alpha = 0.12f) else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (active) color else colors.textHint)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (active) colors.textPrimary else colors.textSecondary
            )
            Text(desc, fontSize = 12.sp, color = colors.textHint)
        }
        if (active) {
            Text("当前", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun PriceTableCard(state: PriceUiState) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .width(360.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                "价格参考（元 / 百万 tokens）",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
            Text(
                if (state.isPeak) "当前适用「高峰」列价格" else "当前适用「空闲」列价格",
                fontSize = 12.sp,
                color = colors.textHint
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "项目",
                fontSize = 12.sp,
                color = colors.textSecondary,
                modifier = Modifier.weight(1.6f)
            )
            Text(
                "高峰",
                fontSize = 12.sp,
                color = OrangeBadge,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
            Text(
                "空闲",
                fontSize = 12.sp,
                color = SuccessGreen,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }

        state.models.forEach { group ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    group.model,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.balancePrimary
                )
                if (group.version.isNotEmpty()) {
                    Text(group.version, fontSize = 11.sp, color = colors.textHint)
                }
                group.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            item.label,
                            fontSize = 13.sp,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1.6f)
                        )
                        Text(
                            item.peak,
                            fontSize = 13.sp,
                            color = if (state.isPeak) OrangeBadge else colors.textHint,
                            fontWeight = if (state.isPeak) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            item.idle,
                            fontSize = 13.sp,
                            color = if (state.isPeak) colors.textHint else SuccessGreen,
                            fontWeight = if (state.isPeak) FontWeight.Normal else FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Text(
            "模型、时段与价格来自 DeepSeek 官网，可能随时调整；点「获取官网数据」可更新，请以官网为准。",
            fontSize = 12.sp,
            color = colors.textHint,
            lineHeight = 16.sp
        )
    }
}

private fun formatWindows(windows: List<PricingWindow>): String {
    if (windows.isEmpty()) return "—"
    return windows.groupBy { it.days }.entries.joinToString("；") { (days, list) ->
        val dayText = formatDays(days)
        val timeText = list.joinToString("、") {
            "${formatMinute(it.startMinute)}–${formatMinute(it.endMinute)}"
        }
        "$dayText $timeText"
    }
}

private fun formatDays(days: List<Int>): String {
    val sorted = days.sorted()
    if (sorted.isEmpty()) return "—"
    if (sorted.size == 7) return "每天"
    val continuous = sorted == (sorted.first()..sorted.last()).toList()
    if (continuous) {
        return if (sorted.first() == sorted.last()) {
            "周${dayNames[sorted.first() - 1]}"
        } else {
            "周${dayNames[sorted.first() - 1]}至周${dayNames[sorted.last() - 1]}"
        }
    }
    return sorted.joinToString("、") { "周${dayNames[it - 1]}" }
}

private fun formatMinute(minute: Int): String =
    "%02d:%02d".format(minute / 60, minute % 60)

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "无法打开浏览器", Toast.LENGTH_SHORT).show()
    }
}

package com.bigfatfish.release.ui.balance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigfatfish.release.data.model.BalanceSnapshot

import com.bigfatfish.release.ui.theme.DangerRed
import com.bigfatfish.release.ui.theme.IndigoBtn
import com.bigfatfish.release.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BalanceHistoryScreen(
    viewModel: BalanceHistoryViewModel,
    onBack: () -> Unit
) {
    val snapshots by viewModel.snapshots.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current
    val textMeasurer = rememberTextMeasurer()

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
                    containerColor = colors.indigoBtn.copy(alpha = colors.btnBgAlpha),
                    contentColor = colors.indigoBtn
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = ButtonDefaults.ContentPadding,
                modifier = Modifier.height(32.dp)
            ) { Text("返回", fontSize = 14.sp) }
            Spacer(Modifier.weight(1f))
            Text("历史趋势", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { viewModel.clear() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.dangerBtn.copy(alpha = colors.btnBgAlpha),
                    contentColor = colors.dangerBtn
                ),
                contentPadding = ButtonDefaults.ContentPadding,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) { Text("清除", fontSize = 14.sp) }
        }

        if (snapshots.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("暂无历史记录", fontSize = 16.sp, color = colors.textSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("余额变化趋势", fontSize = 14.sp, color = colors.textSecondary)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.cardBackground)
                ) {
                    drawChart(snapshots, textMeasurer, colors.balancePrimary)
                }

                val lastBalance = snapshots.lastOrNull()?.totalBalance?.toDoubleOrNull()
                val prevBalance = if (snapshots.size >= 2) snapshots[snapshots.size - 2].totalBalance.toDoubleOrNull() else null
                val diff = if (lastBalance != null && prevBalance != null) lastBalance - prevBalance else null
                if (diff != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("较上次", fontSize = 13.sp, color = colors.textSecondary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when {
                                diff > 0 -> "↑ +" + "%.2f".format(diff)
                                diff < 0 -> "↓ " + "%.2f".format(diff)
                                else -> "持平"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                diff > 0 -> colors.successBtn
                                diff < 0 -> colors.dangerBtn
                                else -> colors.textSecondary
                            }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(snapshots, key = { "${it.timestamp}_${it.totalBalance}" }) { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.cardBackground)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                formatTime(s.timestamp),
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${s.totalBalance} ${s.currency}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.balancePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawChart(snapshots: List<BalanceSnapshot>, textMeasurer: TextMeasurer, lineColor: Color) {
    val w = size.width
    val h = size.height
    if (snapshots.isEmpty() || w == 0f || h == 0f) return

    val padLeft = 30f
    val padRight = 20f
    val padTop = 20f
    val padBottom = 30f
    val chartW = w - padLeft - padRight
    val chartH = h - padTop - padBottom

    var minVal = Double.MAX_VALUE
    var maxVal = Double.MIN_VALUE
    for (s in snapshots) {
        val v = s.totalBalance.toDoubleOrNull()
        if (v != null) {
            if (v < minVal) minVal = v
            if (v > maxVal) maxVal = v
        }
    }
    if (minVal == Double.MAX_VALUE || maxVal == Double.MIN_VALUE) return
    if (maxVal == minVal) maxVal = minVal + 1
    val range = maxVal - minVal
    val pad = range * 0.15
    val yMin = maxOf(0.0, minVal - pad)
    val yMax = maxVal + pad

    val gridRows = 4
    for (i in 0..gridRows) {
        val gy = padTop + (chartH * i) / gridRows
        drawLine(
            color = Color(0xFF888888),
            start = Offset(padLeft, gy),
            end = Offset(padLeft + chartW, gy),
            strokeWidth = 0.5f,
            alpha = 0.3f
        )
    }

    val n = snapshots.size
    val pts = ArrayList<Offset>(n)
    for (i in 0 until n) {
        val v = snapshots[i].totalBalance.toDoubleOrNull() ?: 0.0
        val x = if (n == 1) padLeft + chartW / 2 else padLeft + (chartW * i) / (n - 1)
        val y = (padTop + chartH - ((v - yMin) / (yMax - yMin)) * chartH).toFloat()
        pts.add(Offset(x, y))
    }

    for (i in 1 until n) {
        drawLine(
            color = lineColor,
            start = pts[i - 1],
            end = pts[i],
            strokeWidth = 2f
        )
    }

    for (i in 0 until n) {
        drawCircle(color = lineColor, radius = 3f, center = pts[i])
    }

    if (n > 0) {
        val last = pts[n - 1]
        val layout = textMeasurer.measure(
            text = AnnotatedString(snapshots[n - 1].totalBalance),
            style = TextStyle(fontSize = 12.sp, color = lineColor)
        )
        val labelW = layout.size.width
        val labelH = layout.size.height
        val offset = 6f
        val rightX = last.x + offset
        val x = if (rightX + labelW > w) last.x - offset - labelW else rightX
        val topY = last.y - offset - labelH
        val y = if (topY < 0f) last.y + offset else topY
        drawText(layout, topLeft = Offset(x, y))
    }
}

private fun formatTime(ts: Long): String {
    return SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(ts))
}
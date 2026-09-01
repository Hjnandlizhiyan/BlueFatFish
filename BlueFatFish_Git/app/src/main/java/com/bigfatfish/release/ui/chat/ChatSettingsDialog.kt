package com.bigfatfish.release.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigfatfish.release.ui.theme.LocalAppColors

@Composable
fun ChatSettingsDialog(
    chatViewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val state by chatViewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("聊天设置", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // 上下文条数限制
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("上下文条数限制", fontSize = 14.sp, color = colors.textPrimary)
                        Spacer(Modifier.weight(1f))
                        Text(
                            if (state.contextLimit == 0) "不限" else "${state.contextLimit} 条",
                            fontSize = 14.sp,
                            color = colors.balancePrimary
                        )
                    }
                    Slider(
                        value = state.contextLimit.toFloat(),
                        onValueChange = { chatViewModel.updateContextLimit(it.toInt()) },
                        valueRange = 0f..50f
                    )
                }

                // 保留历史对话
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("保留历史对话", fontSize = 14.sp, color = colors.textPrimary)
                        Spacer(Modifier.weight(1f))
                        Text(
                            if (state.historyCount == 0) "无限" else "保留 ${state.historyCount} 段",
                            fontSize = 14.sp,
                            color = colors.balancePrimary
                        )
                    }
                    val sliderValue = if (state.historyCount == 0) 0f else (31 - state.historyCount).toFloat()
                    Slider(
                        value = sliderValue,
                        onValueChange = { v ->
                            val intV = v.toInt()
                            chatViewModel.updateHistoryCount(if (intV == 0) 0 else 31 - intV)
                        },
                        valueRange = 0f..30f
                    )
                }

                // 请求超时
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("请求超时", fontSize = 14.sp, color = colors.textPrimary)
                        Spacer(Modifier.weight(1f))
                        Text("${state.timeoutSeconds} 秒", fontSize = 14.sp, color = colors.balancePrimary)
                    }
                    Slider(
                        value = state.timeoutSeconds.toFloat(),
                        onValueChange = { chatViewModel.updateTimeoutSeconds(it.toInt()) },
                        valueRange = 10f..120f
                    )
                }

                // 默认模型
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("默认模型", fontSize = 14.sp, color = colors.textPrimary)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ModelButton(
                            label = "deepseek-chat",
                            selected = state.defaultModel == "deepseek-chat",
                            colors = colors
                        ) { chatViewModel.setDefaultModel("deepseek-chat") }
                        ModelButton(
                            label = "deepseek-reasoner",
                            selected = state.defaultModel == "deepseek-reasoner",
                            colors = colors
                        ) { chatViewModel.setDefaultModel("deepseek-reasoner") }
                    }
                }

                // 深度思考默认开启
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("深度思考默认开启", fontSize = 14.sp, color = colors.textPrimary)
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = state.deepThinkDefault,
                        onCheckedChange = { chatViewModel.setDeepThinkDefault(it) }
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        confirmButton = {
            TextButton(onClick = {
                chatViewModel.saveSettings()
                onDismiss()
            }) { Text("完成") }
        }
    )
}

@Composable
private fun ModelButton(
    label: String,
    selected: Boolean,
    colors: com.bigfatfish.release.ui.theme.AppColors,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) colors.balancePrimary else colors.cardBackground,
            contentColor = if (selected) Color.White else colors.textPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(label, fontSize = 13.sp)
    }
}
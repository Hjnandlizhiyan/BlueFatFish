package com.bigfatfish.release.ui.keylist

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigfatfish.release.data.model.KeyMeta
import com.bigfatfish.release.ui.theme.BlueGreyBtn
import com.bigfatfish.release.ui.theme.DangerRed
import com.bigfatfish.release.ui.theme.GreenBadge
import com.bigfatfish.release.ui.theme.LocalAppColors

@Composable
fun KeyListScreen(
    viewModel: KeyListViewModel,
    onBack: () -> Unit
) {
    val keys by viewModel.keys.collectAsStateWithLifecycle()
    val currentKeyId by viewModel.currentKeyId.collectAsStateWithLifecycle()
    val consumption by viewModel.consumption.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    var deleteTarget by remember { mutableStateOf<KeyMeta?>(null) }

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
                    containerColor = colors.blueGreyBtn.copy(alpha = colors.btnBgAlpha),
                    contentColor = colors.blueGreyBtn
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = ButtonDefaults.ContentPadding,
                modifier = Modifier.height(32.dp)
            ) { Text("返回", fontSize = 14.sp) }
            Spacer(Modifier.weight(1f))
            Text("Key 列表", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))
        }

        if (keys.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("暂无已保存的 Key", fontSize = 16.sp, color = colors.textSecondary)
                Text("请返回主页添加", fontSize = 13.sp, color = colors.textHint)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(keys, key = { it.id }) { meta ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.cardBackground)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(meta.note, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                            Spacer(Modifier.weight(1f))
                            if (meta.id == currentKeyId) {
                                Text(
                                    "当前使用",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GreenBadge)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(meta.masked, fontSize = 13.sp, color = colors.textSecondary, textAlign = TextAlign.Start)
                        val change = consumption[meta.id]
                        if (change != null) {
                            Text(
                                text = when {
                                    change < 0 -> "累计消耗 " + "%.2f".format(-change)
                                    change > 0 -> "余额增长 +" + "%.2f".format(change)
                                    else -> "余额持平"
                                },
                                fontSize = 12.sp,
                                color = when {
                                    change < 0 -> colors.dangerBtn
                                    change > 0 -> colors.successBtn
                                    else -> colors.textSecondary
                                }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.switchKey(meta) },
                                enabled = meta.id != currentKeyId,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.balancePrimary.copy(alpha = colors.btnBgAlpha),
                                    contentColor = colors.balancePrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text(if (meta.id == currentKeyId) "已使用" else "切换") }
                            Button(
                                onClick = { deleteTarget = meta },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.dangerBtn.copy(alpha = colors.btnBgAlpha),
                                    contentColor = colors.dangerBtn
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text("删除") }
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { meta ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除确认") },
            text = { Text("确定删除该 Key（${meta.note}）吗？") },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(meta)
                    deleteTarget = null
                }) { Text("删除", color = DangerRed) }
            }
        )
    }
}
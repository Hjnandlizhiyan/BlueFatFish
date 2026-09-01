package com.bigfatfish.release.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigfatfish.release.data.AppJson
import com.bigfatfish.release.data.model.ChatOpenRequest
import com.bigfatfish.release.data.model.ChatSession
import com.bigfatfish.release.navigation.NavigationBus
import com.bigfatfish.release.ui.theme.DangerRed
import com.bigfatfish.release.ui.theme.IndigoBtn
import com.bigfatfish.release.ui.theme.LocalAppColors
import com.bigfatfish.release.ui.theme.SuccessGreen
import kotlinx.serialization.encodeToString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatHistoryScreen(
    viewModel: ChatHistoryViewModel,
    onBack: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    var renamingId by remember { mutableStateOf("") }
    var renameText by remember { mutableStateOf("") }
    var showRename by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<ChatSession?>(null) }

    fun openSession(s: ChatSession) {
        NavigationBus.chatOpenRequest.value = AppJson.encodeToString(ChatOpenRequest("open", s.id))
        onBack()
    }

    fun newChat() {
        NavigationBus.chatOpenRequest.value = AppJson.encodeToString(ChatOpenRequest("new", ""))
        onBack()
    }

    fun openRename(s: ChatSession) {
        renamingId = s.id
        renameText = s.title
        showRename = true
    }

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
            Text("历史对话", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { newChat() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.successBtn.copy(alpha = colors.btnBgAlpha),
                    contentColor = colors.successBtn
                ),
                contentPadding = ButtonDefaults.ContentPadding,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) { Text("新建对话", fontSize = 12.sp) }
        }

        if (sessions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("暂无历史对话", fontSize = 16.sp, color = colors.textSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions, key = { it.id }) { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cardBackground)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (s.title.isEmpty()) "（无标题）" else s.title,
                                fontSize = 14.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${s.messages.size} 条消息 · ${formatTime(s.updatedAt)}",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                        Button(
                            onClick = { openSession(s) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.balancePrimary.copy(alpha = colors.btnBgAlpha),
                                contentColor = colors.balancePrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        ) { Text("打开", fontSize = 11.sp) }
                        Button(
                            onClick = { openRename(s) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.balancePrimary.copy(alpha = colors.btnBgAlpha),
                                contentColor = colors.balancePrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp).padding(start = 6.dp)
                        ) { Text("重命名", fontSize = 11.sp) }
                        Button(
                            onClick = { deleteTarget = s },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.dangerBtn.copy(alpha = colors.btnBgAlpha),
                                contentColor = colors.dangerBtn
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp).padding(start = 6.dp)
                        ) { Text("删除", fontSize = 11.sp) }
                    }
                }
            }
        }
    }

    if (showRename) {
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text("重命名对话", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    placeholder = { Text("输入对话名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                TextButton(onClick = { showRename = false }) { Text("取消") }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rename(renamingId, renameText)
                    showRename = false
                }) { Text("确定") }
            }
        )
    }

    deleteTarget?.let { s ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除对话") },
            text = { Text("确定删除「${if (s.title.isEmpty()) "无标题" else s.title}」吗？") },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(s.id)
                    deleteTarget = null
                }) { Text("删除", color = DangerRed) }
            }
        )
    }
}

private fun formatTime(ts: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(ts))
}
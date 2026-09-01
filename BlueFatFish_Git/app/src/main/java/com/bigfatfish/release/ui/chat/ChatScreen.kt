package com.bigfatfish.release.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigfatfish.release.data.model.ChatMessage
import com.bigfatfish.release.ui.theme.DangerRed
import com.bigfatfish.release.ui.theme.GreenBadge
import com.bigfatfish.release.ui.theme.LocalAppColors


@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    onOpenHistory: () -> Unit
) {
    val state by chatViewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    var showClearDialog by remember { mutableStateOf(false) }
    var topExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.pageBackground)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("AI 聊天", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { chatViewModel.openSettingsDialog() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.balancePrimary.copy(alpha = colors.btnBgAlpha),
                    contentColor = colors.balancePrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(30.dp)
            ) { Text("设置", fontSize = 12.sp) }
            Button(
                onClick = { topExpanded = !topExpanded },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.balancePrimary.copy(alpha = colors.btnBgAlpha),
                    contentColor = colors.balancePrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(30.dp)
            ) { Text(if (topExpanded) "收起" else "放下", fontSize = 12.sp) }
        }

        if (topExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { chatViewModel.newChat() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.balancePrimary.copy(alpha = colors.btnBgAlpha),
                        contentColor = colors.balancePrimary
                    ),
                    modifier = Modifier.weight(1f).height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = ButtonDefaults.ContentPadding
                ) { Text("新对话", fontSize = 12.sp) }
                Button(
                    onClick = onOpenHistory,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.balancePrimary.copy(alpha = colors.btnBgAlpha),
                        contentColor = colors.balancePrimary
                    ),
                    modifier = Modifier.weight(1f).height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = ButtonDefaults.ContentPadding
                ) { Text("历史", fontSize = 12.sp) }
                Button(
                    onClick = { showClearDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.dangerBtn.copy(alpha = colors.btnBgAlpha),
                        contentColor = colors.dangerBtn
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = ButtonDefaults.ContentPadding
                ) { Text("清空对话", fontSize = 12.sp) }
            }

            Text(
                text = "开启「保留历史对话」后，对话会保存到本地，可在「历史」中查看",
                fontSize = 11.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            // ---------- API Key 确认区 ----------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.apiKeyInput,
                    onValueChange = { chatViewModel.updateApiKeyInput(it) },
                    placeholder = { Text("输入 DeepSeek API Key") },
                    enabled = !state.keyConfirmed,
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.keyConfirmed) {
                        Text(
                            text = "已确认",
                            fontSize = 12.sp,
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(GreenBadge)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        Button(
                            onClick = { chatViewModel.resetKey() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.balancePrimary.copy(alpha = colors.btnBgAlpha),
                                contentColor = colors.balancePrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        ) { Text("重新输入", fontSize = 12.sp) }
                    } else {
                        Button(
                            onClick = { chatViewModel.confirmKey() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.balancePrimary.copy(alpha = colors.btnBgAlpha),
                                contentColor = colors.balancePrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        ) { Text("确认使用", fontSize = 12.sp) }
                    }
                }
            }
        }

        // ---------- 聊天列表 ----------
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(state.messages, key = { index, msg -> msg.id ?: "${index}_${msg.content}" }) { index, msg ->
                MessageItem(msg, index, state.expandedMsgIndex, colors, chatViewModel::toggleReasoning)
            }
        }

        // ---------- 输入区 ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val deepThinkColor = if (state.deepThink)
                    colors.balancePrimary.copy(alpha = 0.22f)
                else
                    colors.textSecondary.copy(alpha = 0.22f)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(deepThinkColor)
                        .clickable { chatViewModel.setDeepThink(!state.deepThink) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "深度思考",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (state.deepThink) colors.balancePrimary else colors.textSecondary
                    )
                }
                if (state.deepThink) {
                    Text("deepseek-reasoner", fontSize = 12.sp, color = colors.textSecondary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = state.inputText,
                    onValueChange = { chatViewModel.updateInputText(it) },
                    placeholder = { Text("输入消息...") },
                    minLines = 1,
                    maxLines = 4,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { chatViewModel.send() },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.balancePrimary.copy(alpha = colors.btnBgAlpha),
                        contentColor = colors.balancePrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(44.dp)
                ) { Text(if (state.isLoading) "发送中" else "发送") }
            }
        }
    }

    // ---------- 清空确认弹窗 ----------
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清除对话") },
            text = { Text("确定清除所有对话记录吗？") },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    chatViewModel.doClearChat()
                }) {
                    Text("清除", color = DangerRed)
                }
            }
        )
    }

    // ---------- 聊天设置弹窗 ----------
    if (state.showSettingsDialog) {
        ChatSettingsDialog(
            chatViewModel = chatViewModel,
            onDismiss = { chatViewModel.closeSettingsDialog() }
        )
    }
}

@Composable
private fun MessageItem(
    msg: ChatMessage,
    index: Int,
    expandedMsgIndex: Int,
    colors: com.bigfatfish.release.ui.theme.AppColors,
    onToggleReasoning: (Int) -> Unit
) {
    val isUser = msg.role == "user"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!isUser && msg.reasoning_content != null && msg.reasoning_content.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.reasoningBackground)
                    .padding(8.dp)
                    .clickable { onToggleReasoning(index) },
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("深度思考", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (expandedMsgIndex == index) "收起" else "展开",
                        fontSize = 12.sp,
                        color = colors.balancePrimary
                    )
                }
                if (expandedMsgIndex == index) {
                    Text(msg.reasoning_content, fontSize = 12.sp, color = colors.textSecondary)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            if (isUser) Spacer(Modifier.weight(1f))
            Text(
                text = msg.content,
                fontSize = 14.sp,
                color = if (isUser) androidx.compose.ui.graphics.Color.White else colors.textPrimary,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isUser) colors.balancePrimary else colors.cardBackground)
                    .padding(10.dp),
                textAlign = TextAlign.Start
            )
            if (!isUser) Spacer(Modifier.weight(1f))
        }
    }
}
package com.bigfatfish.release.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bigfatfish.release.R
import com.bigfatfish.release.data.local.CheckInStore
import com.bigfatfish.release.data.model.BalanceInfo
import com.bigfatfish.release.ui.chat.ChatScreen
import com.bigfatfish.release.ui.chat.ChatViewModel
import com.bigfatfish.release.ui.theme.BlueGreyBtn
import com.bigfatfish.release.ui.theme.DangerRed
import com.bigfatfish.release.ui.theme.GreenBadge
import com.bigfatfish.release.ui.theme.IndigoBtn
import com.bigfatfish.release.ui.theme.LocalAppColors
import com.bigfatfish.release.ui.theme.OrangeBadge
import com.bigfatfish.release.ui.theme.PlushRed
import com.bigfatfish.release.ui.theme.SuccessGreen
import com.bigfatfish.release.ui.theme.TealBtn
import com.bigfatfish.release.ui.theme.UserBubbleBlue
import androidx.compose.ui.graphics.RectangleShape

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    onNavigate: (String) -> Unit
) {
    val chatViewModel: ChatViewModel = viewModel()
    val state by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        dashboardViewModel.onResume()
        onPauseOrDispose { }
    }

    var showTopUpDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.pageBackground)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            TabItem("余额查询", 0, state.currentIndex) { dashboardViewModel.setCurrentIndex(0) }
            TabItem("AI 聊天", 1, state.currentIndex) { dashboardViewModel.setCurrentIndex(1) }
        }

        when (state.currentIndex) {
            0 -> BalanceTab(state, colors, dashboardViewModel, onNavigate) { showTopUpDialog = true }
            1 -> ChatScreen(chatViewModel, onOpenHistory = { onNavigate("chat_history") })
        }
    }

    if (state.showNetworkNotice) {
        AlertDialog(
            onDismissRequest = { dashboardViewModel.dismissNetworkNotice() },
            title = { Text("联网说明") },
            text = { Text("本应用查询余额、AI 聊天及参团跳转等功能均需联网，请确保网络连接正常。") },
            confirmButton = {
                TextButton(onClick = { dashboardViewModel.dismissNetworkNotice() }) { Text("知道了") }
            }
        )
    }

    if (showTopUpDialog) {
        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            title = { Text("跳转确认") },
            text = { Text("即将打开浏览器访问 DeepSeek 官网充值页面，是否继续？") },
            dismissButton = {
                TextButton(onClick = { showTopUpDialog = false }) { Text("取消") }
            },
            confirmButton = {
                TextButton(onClick = {
                    showTopUpDialog = false
                    openUrl(context, "https://platform.deepseek.com/top_up")
                }) { Text("继续") }
            }
        )
    }
}

@Composable
private fun TabItem(label: String, index: Int, current: Int, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Text(
        text = label,
        fontSize = 16.sp,
        fontWeight = if (current == index) FontWeight.Bold else FontWeight.Normal,
        color = if (current == index) colors.balancePrimary else colors.textSecondary,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable { onClick() }
    )
}

@Composable
private fun BalanceTab(
    state: DashboardUiState,
    colors: com.bigfatfish.release.ui.theme.AppColors,
    viewModel: DashboardViewModel,
    onNavigate: (String) -> Unit,
    onTopUp: () -> Unit
) {
    var showEgg by remember { mutableStateOf(false) }
    var eggCount by remember { mutableStateOf(0) }
    var eggLast by remember { mutableStateOf(0L) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DeepSeek 余额查询",
            fontSize = 24.sp,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp)
        )

        // ---------- API Key 卡片 ----------
        Column(
            modifier = Modifier
                .width(360.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(colors.cardBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("API Key", fontSize = 16.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                if (state.hasBound) {
                    Badge("已绑定", GreenBadge)
                } else {
                    Badge("未绑定", OrangeBadge)
                }
            }

            if (state.hasBound) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (state.currentNote.isNotEmpty()) state.currentNote + "：" + viewModel.displayKey()
                        else viewModel.displayKey(),
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                    Button(
                        onClick = { viewModel.toggleShowKey() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.errorBtnBg,
                            contentColor = colors.errorBtnText
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(if (state.showPlainKey) "隐藏" else "显示", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { viewModel.copyKey() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.successBtn.copy(alpha = colors.btnBgAlpha),
                            contentColor = colors.successBtn
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("复制", fontSize = 12.sp)
                    }
                }
            }

            OutlinedTextField(
                value = state.apiKeyInput,
                onValueChange = { viewModel.updateApiKeyInput(it) },
                placeholder = { Text("粘贴你的 DeepSeek API Key") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.noteInput,
                onValueChange = { viewModel.updateNoteInput(it) },
                placeholder = { Text("备注（如：生产 / 测试 / 项目A）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.saveKey() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.balancePrimary.copy(alpha = colors.btnBgAlpha),
                        contentColor = colors.balancePrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("保存") }
                Button(
                    onClick = { onNavigate("key_list") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.blueGreyBtn.copy(alpha = colors.btnBgAlpha),
                        contentColor = colors.blueGreyBtn
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("Key 列表") }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onNavigate("history") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.indigoBtn.copy(alpha = colors.btnBgAlpha),
                        contentColor = colors.indigoBtn
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("历史趋势") }
                Button(
                    onClick = { onNavigate("settings") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.tealBtn.copy(alpha = colors.btnBgAlpha),
                        contentColor = colors.tealBtn
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("设置") }
            }
        }

        // ---------- 总余额卡片 ----------
        Column(
            modifier = Modifier
                .width(360.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(colors.cardBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${state.totalBalance} ${state.currency}",
                fontSize = 36.sp,
                color = colors.balancePrimary,
                fontWeight = FontWeight.Bold
            )
            Text("总余额", fontSize = 14.sp, color = colors.textSecondary)
        }


        // ---------- 多币种列表 ----------
        if (state.balances.size > 1) {
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.cardBackground)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("多币种余额", fontSize = 16.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                state.balances.forEach { info: BalanceInfo ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(info.currency, fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                        Text(
                            "总 ${info.total_balance}",
                            fontSize = 14.sp,
                            color = colors.balancePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ---------- 充值入口 ----------
        Button(
            onClick = onTopUp,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.successBtn.copy(alpha = colors.btnBgAlpha),
                contentColor = colors.successBtn
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .width(360.dp)
                .height(44.dp)
        ) { Text("去 DeepSeek 官网充值", fontSize = 16.sp) }

        // ---------- 参团入口 ----------
        if (state.settings.showGroupBuy) {
            Button(
                onClick = { onNavigate("group_buy") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.plushBtn.copy(alpha = colors.btnBgAlpha),
                    contentColor = colors.plushBtn
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(360.dp)
                    .height(44.dp)
            ) { Text("玩偶参团", fontSize = 16.sp) }
        }

        // ---------- 刷新余额 ----------
        Button(
            onClick = { viewModel.refreshBalance() },
            enabled = !state.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.balancePrimary.copy(alpha = colors.btnBgAlpha),
                contentColor = colors.balancePrimary
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .width(360.dp)
                .height(44.dp)
        ) { Text(if (state.isLoading) "查询中..." else "刷新余额") }

        // ---------- 错误详情 ----------
        if (state.errorDetail.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.errorBg)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("错误详情", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.errorTitle)
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { viewModel.clearError() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.errorBtnBg,
                            contentColor = colors.errorBtnText
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) { Text("清空", fontSize = 12.sp) }
                }
                Text(state.errorDetail, fontSize = 12.sp, color = colors.errorText, textAlign = TextAlign.Start)
            }
        }

        // ---------- 签到打卡 ----------
        Column(
            modifier = Modifier
                .width(360.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(colors.cardBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("签到打卡", fontSize = 16.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("连续打卡 ${state.checkIn.streak} 天", fontSize = 14.sp, color = colors.textSecondary)
                Spacer(Modifier.weight(1f))
                Text(
                    CheckInStore.title(state.checkIn.streak),
                    fontSize = 14.sp,
                    color = colors.balancePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("累计打卡 ${state.checkIn.total} 天 · 每天查余额自动打卡", fontSize = 12.sp, color = colors.textHint)
        }

        // ---------- 底部图标 ----------
        if (state.settings.showIcon) {
            val icon = if (state.iconIndex == 0) R.drawable.deepseek_icon_01 else R.drawable.deepseek_icon_02
            androidx.compose.foundation.Image(
                painter = painterResource(id = icon),
                contentDescription = "首页图标",
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        val now = System.currentTimeMillis()
                        eggCount = if (now - eggLast < 1500L) eggCount + 1 else 1
                        eggLast = now
                        if (eggCount >= 5) {
                            eggCount = 0
                            showEgg = true
                        } else {
                            viewModel.toggleIcon()
                        }
                    }
            )
        }

        // ---------- 底部商标 ----------
        Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp)) {
            Text(
                text = "© 2026 蓝色大肥鱼",
                fontSize = 8.sp,
                color = colors.textHint,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.cardBackground)
            )
        }

        Spacer(Modifier.height(20.dp))
    }

    if (showEgg) {
        AlertDialog(
            onDismissRequest = { showEgg = false },
            title = { Text("🐋哦鲸鲸！") },
            text = { Text("你发现了隐藏的大肥鱼！\n大肥鱼祝你余额多多、天天爆单～") },
            confirmButton = {
                TextButton(onClick = { showEgg = false }) { Text("收下祝福") }
            }
        )
    }
}

@Composable
private fun Badge(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = androidx.compose.ui.graphics.Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "无法打开浏览器", Toast.LENGTH_SHORT).show()
    }
}
package com.bigfatfish.release.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigfatfish.release.ui.theme.AppColors
import com.bigfatfish.release.ui.theme.LocalAppColors
import com.bigfatfish.release.ui.theme.TealBtn

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

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
                contentPadding = ButtonDefaults.ContentPadding,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) { Text("返回", fontSize = 14.sp) }
            Spacer(Modifier.weight(1f))
            Text("设置", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- 主题 ----------
            SettingCard(colors) {
                Text("主题", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOption("跟随系统", "system", settings.themeMode, colors) { viewModel.updateThemeMode("system") }
                    ThemeOption("浅色", "light", settings.themeMode, colors) { viewModel.updateThemeMode("light") }
                    ThemeOption("深色", "dark", settings.themeMode, colors) { viewModel.updateThemeMode("dark") }
                }
            }

            // ---------- 主题皮肤 ----------
            SettingCard(colors) {
                Text("主题皮肤", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOption("经典蓝", "default", settings.skin, colors) { viewModel.updateSkin("default") }
                    ThemeOption("大肥鱼蓝", "fishBlue", settings.skin, colors) { viewModel.updateSkin("fishBlue") }
                }
            }

            // ---------- 低余额阈值 ----------
            SettingCard(colors) {
                Text("低余额阈值", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                OutlinedTextField(
                    value = settings.threshold,
                    onValueChange = { viewModel.updateThreshold(it) },
                    placeholder = { Text("输入阈值（如 10），留空关闭提醒") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("低余额提醒", fontSize = 14.sp, color = colors.textPrimary)
                    Spacer(Modifier.weight(1f))
                    Switch(checked = settings.alertEnabled, onCheckedChange = { viewModel.updateAlertEnabled(it) })
                }
            }

            // ---------- Key 显示 ----------
            SettingCard(colors) {
                Text("Key 显示", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("默认遮罩 Key（关闭则显示明文）", fontSize = 14.sp, color = colors.textPrimary)
                    Spacer(Modifier.weight(1f))
                    Switch(checked = settings.maskKey, onCheckedChange = { viewModel.updateMaskKey(it) })
                }
            }

            // ---------- 首页表情 ----------
            SettingCard(colors) {
                Text("首页表情", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("显示首页右下角表情", fontSize = 14.sp, color = colors.textPrimary)
                    Spacer(Modifier.weight(1f))
                    Switch(checked = settings.showIcon, onCheckedChange = { viewModel.updateShowIcon(it) })
                }
            }

            // ---------- 玩偶参团 ----------
            SettingCard(colors) {
                Text("玩偶参团", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("显示首页玩偶参团入口", fontSize = 14.sp, color = colors.textPrimary)
                    Spacer(Modifier.weight(1f))
                    Switch(checked = settings.showGroupBuy, onCheckedChange = { viewModel.updateShowGroupBuy(it) })
                }
            }

            Button(
                onClick = { viewModel.save() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.tealBtn.copy(alpha = colors.btnBgAlpha),
                    contentColor = colors.tealBtn
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) { Text("保存设置", fontSize = 16.sp) }

            Text(
                text = "官方QQ群：305402575",
                fontSize = 14.sp,
                color = colors.textSecondary
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingCard(colors: AppColors, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.cardBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun RowScope.ThemeOption(
    label: String,
    mode: String,
    selected: String,
    colors: AppColors,
    onClick: () -> Unit
) {
    Text(
        text = label,
        fontSize = 14.sp,
        color = if (selected == mode) Color.White else colors.textSecondary,
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected == mode) TealBtn else colors.pageBackground)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        textAlign = TextAlign.Center
    )
}
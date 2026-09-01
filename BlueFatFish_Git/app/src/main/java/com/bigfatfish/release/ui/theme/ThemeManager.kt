package com.bigfatfish.release.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 全局主题状态：'system' | 'light' | 'dark'。
 * 由设置页写入，MainActivity 读取并驱动 Compose 主题。
 */
object ThemeManager {
    val themeMode = MutableStateFlow("system")
    val skin = MutableStateFlow("default")
}
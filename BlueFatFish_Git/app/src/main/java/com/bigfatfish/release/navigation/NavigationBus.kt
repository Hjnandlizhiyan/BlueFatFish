package com.bigfatfish.release.navigation

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 全局状态总线（替代鸿蒙 AppStorage）：
 * 聊天历史页回传「打开/新建」指令给首页聊天区。
 */
object NavigationBus {
    val chatOpenRequest = MutableStateFlow("")
}
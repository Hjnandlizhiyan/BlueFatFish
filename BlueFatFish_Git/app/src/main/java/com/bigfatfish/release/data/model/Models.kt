package com.bigfatfish.release.data.model

import kotlinx.serialization.Serializable

// ===== 余额接口模型 =====
@Serializable
data class BalanceInfo(
    val currency: String,
    val total_balance: String,
    val granted_balance: String,
    val topped_up_balance: String
)

@Serializable
data class BalanceResponse(
    val is_available: Boolean,
    val balance_infos: List<BalanceInfo> = emptyList()
)

// ===== 聊天接口模型 =====
@Serializable
data class ChatMessage(
    val id: String? = null,
    val role: String,
    val content: String,
    val reasoning_content: String? = null
)

@Serializable
data class ChatRequestBody(
    val model: String,
    val messages: List<ChatMessage>
)

@Serializable
data class ChatChoice(
    val message: ChatMessage
)

@Serializable
data class ChatResponse(
    val choices: List<ChatChoice> = emptyList()
)

// 聊天结果（结构化返回，供页面组装 assistant 消息）
data class ChatResult(
    val content: String,
    val reasoning_content: String
)

// 聊天设置
@Serializable
data class ChatSettings(
    val contextLimit: Int,
    val historyCount: Int,
    val timeoutSeconds: Int,
    val defaultModel: String,
    val deepThinkDefault: Boolean
)

// 历史对话会话
@Serializable
data class ChatSession(
    val id: String,
    val title: String,
    val messages: List<ChatMessage>,
    val updatedAt: Long
)

// 历史页回传聊天页的指令
@Serializable
data class ChatOpenRequest(
    val action: String,
    val id: String
)

// ===== 应用扩展模型 =====
@Serializable
data class KeyMeta(
    val id: String,
    val note: String,
    val masked: String,
    val createdAt: Long
)

@Serializable
data class BalanceSnapshot(
    val timestamp: Long,
    val totalBalance: String,
    val currency: String,
    val keyId: String = ""
)

@Serializable
data class AppSettings(
    val themeMode: String,
    val threshold: String,
    val alertEnabled: Boolean,
    val maskKey: Boolean,
    val showIcon: Boolean,
    val showGroupBuy: Boolean,
    val skin: String = "default"
)

@Serializable
data class CheckInState(
    val lastDate: String = "",
    val streak: Int = 0,
    val total: Int = 0
)
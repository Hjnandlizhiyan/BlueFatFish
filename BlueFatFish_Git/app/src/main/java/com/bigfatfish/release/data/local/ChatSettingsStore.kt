package com.bigfatfish.release.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bigfatfish.release.data.AppJson
import com.bigfatfish.release.data.model.ChatSettings
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

object ChatSettingsStore {
    private val KEY_SETTINGS = stringPreferencesKey("chat_settings")

    fun defaultSettings() = ChatSettings(
        contextLimit = 20,
        historyCount = 0,
        timeoutSeconds = 60,
        defaultModel = "deepseek-flash",
        deepThinkDefault = false
    )

    suspend fun get(context: Context): ChatSettings {
        return try {
            val raw = context.chatSettingsDataStore.data.first()[KEY_SETTINGS] ?: ""
            if (raw.isEmpty()) defaultSettings()
            else {
                val s = AppJson.decodeFromString<ChatSettings>(raw)
                val d = defaultSettings()
                // 兼容旧模型名：deepseek-chat / deepseek-reasoner 已改为 deepseek-flash + thinking 参数
                val migratedModel = when (s.defaultModel) {
                    "deepseek-flash", "deepseek-v4-pro" -> s.defaultModel
                    "deepseek-chat", "deepseek-reasoner" -> "deepseek-flash"
                    else -> d.defaultModel
                }
                ChatSettings(
                    contextLimit = s.contextLimit,
                    historyCount = s.historyCount,
                    timeoutSeconds = s.timeoutSeconds,
                    defaultModel = migratedModel,
                    deepThinkDefault = s.deepThinkDefault || s.defaultModel == "deepseek-reasoner"
                )
            }
        } catch (e: Exception) {
            defaultSettings()
        }
    }

    suspend fun save(context: Context, settings: ChatSettings) {
        try {
            context.chatSettingsDataStore.edit { it[KEY_SETTINGS] = AppJson.encodeToString(settings) }
        } catch (e: Exception) {
        }
    }
}
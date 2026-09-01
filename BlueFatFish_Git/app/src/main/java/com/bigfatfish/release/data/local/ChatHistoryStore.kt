package com.bigfatfish.release.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bigfatfish.release.data.AppJson
import com.bigfatfish.release.data.model.ChatSession
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

object ChatHistoryStore {
    private val KEY_SESSIONS = stringPreferencesKey("sessions")

    suspend fun list(context: Context): List<ChatSession> {
        return try {
            val raw = context.chatHistoryDataStore.data.first()[KEY_SESSIONS] ?: ""
            if (raw.isEmpty()) emptyList()
            else AppJson.decodeFromString<List<ChatSession>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun save(context: Context, sessions: List<ChatSession>) {
        try {
            context.chatHistoryDataStore.edit { it[KEY_SESSIONS] = AppJson.encodeToString(sessions) }
        } catch (e: Exception) {
        }
    }
}
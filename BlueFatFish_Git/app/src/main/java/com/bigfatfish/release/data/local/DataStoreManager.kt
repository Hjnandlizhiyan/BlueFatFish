package com.bigfatfish.release.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.keyDataStore by preferencesDataStore("deepseek_keys")
val Context.chatHistoryDataStore by preferencesDataStore("chat_history")
val Context.chatSettingsDataStore by preferencesDataStore("chat_settings")
val Context.settingsDataStore by preferencesDataStore("deepseek_settings")
val Context.balanceHistoryDataStore by preferencesDataStore("deepseek_history")
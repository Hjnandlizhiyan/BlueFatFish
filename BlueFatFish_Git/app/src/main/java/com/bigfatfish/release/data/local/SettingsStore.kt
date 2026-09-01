package com.bigfatfish.release.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bigfatfish.release.data.AppJson
import com.bigfatfish.release.data.model.AppSettings
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

object SettingsStore {
    private val KEY_SETTINGS = stringPreferencesKey("settings")
    private val KEY_NETWORK_NOTICE = booleanPreferencesKey("network_notice_shown")

    fun defaultSettings() = AppSettings("system", "", true, true, false, false, "default")

    suspend fun get(context: Context): AppSettings {
        return try {
            val raw = context.settingsDataStore.data.first()[KEY_SETTINGS] ?: ""
            if (raw.isEmpty()) defaultSettings()
            else {
                val s = AppJson.decodeFromString<AppSettings>(raw)
                val d = defaultSettings()
                AppSettings(
                    themeMode = if (s.themeMode == "light" || s.themeMode == "dark") s.themeMode else d.themeMode,
                    threshold = s.threshold,
                    alertEnabled = s.alertEnabled,
                    maskKey = s.maskKey,
                    showIcon = s.showIcon,
                    showGroupBuy = s.showGroupBuy,
                    skin = if (s.skin == "default" || s.skin == "fishBlue") s.skin else d.skin
                )
            }
        } catch (e: Exception) {
            defaultSettings()
        }
    }

    suspend fun save(context: Context, settings: AppSettings) {
        context.settingsDataStore.edit { it[KEY_SETTINGS] = AppJson.encodeToString(settings) }
    }

    suspend fun hasShownNetworkNotice(context: Context): Boolean {
        return try {
            context.settingsDataStore.data.first()[KEY_NETWORK_NOTICE] ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun markNetworkNoticeShown(context: Context) {
        try {
            context.settingsDataStore.edit { it[KEY_NETWORK_NOTICE] = true }
        } catch (e: Exception) {
        }
    }
}
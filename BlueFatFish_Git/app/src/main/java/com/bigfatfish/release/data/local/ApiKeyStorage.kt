package com.bigfatfish.release.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Key 明文存储（加密），支持多 Key：key 带唯一 keyId。
 * 兼容旧版单 Key（key = deepseek_api_key）。
 */
object ApiKeyStorage {
    private const val LEGACY_ALIAS = "deepseek_api_key"
    private const val PREF_NAME = "deepseek_keys_secure"

    @Volatile
    private var cachedPrefs: SharedPreferences? = null

    private fun aliasOf(keyId: String): String = "deepseek_api_key_$keyId"

    private fun getPrefs(context: Context): SharedPreferences {
        cachedPrefs?.let { return it }
        val appContext = context.applicationContext
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            appContext,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        cachedPrefs = prefs
        return prefs
    }

    fun save(context: Context, keyId: String, apiKey: String) {
        getPrefs(context).edit().putString(aliasOf(keyId), apiKey).apply()
    }

    fun load(context: Context, keyId: String): String {
        return getPrefs(context).getString(aliasOf(keyId), "") ?: ""
    }

    fun remove(context: Context, keyId: String) {
        getPrefs(context).edit().remove(aliasOf(keyId)).apply()
    }

    fun loadLegacy(context: Context): String {
        return getPrefs(context).getString(LEGACY_ALIAS, "") ?: ""
    }

    fun removeLegacy(context: Context) {
        getPrefs(context).edit().remove(LEGACY_ALIAS).apply()
    }
}
package com.bigfatfish.release.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bigfatfish.release.data.AppJson
import com.bigfatfish.release.data.model.KeyMeta
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * 多 Key 管理器：Key 明文存加密存储，元数据与当前选中 Key 存 DataStore。
 */
object KeyManager {
    private val KEY_META_LIST = stringPreferencesKey("key_meta_list")
    private val KEY_CURRENT_ID = stringPreferencesKey("current_key_id")

    private fun genId(): String {
        return System.currentTimeMillis().toString(36) + "_" +
            ((Math.random() * 10000).toInt()).toString(36)
    }

    private fun maskKey(key: String): String {
        return if (key.length <= 10) key.substring(0, 3) + "****"
        else key.substring(0, 5) + "****" + key.substring(key.length - 4)
    }

    suspend fun listKeys(context: Context): List<KeyMeta> {
        return try {
            val raw = context.keyDataStore.data.first()[KEY_META_LIST] ?: "[]"
            AppJson.decodeFromString<List<KeyMeta>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addKey(context: Context, apiKey: String, note: String): KeyMeta {
        val meta = KeyMeta(
            id = genId(),
            note = note,
            masked = maskKey(apiKey),
            createdAt = System.currentTimeMillis()
        )
        ApiKeyStorage.save(context, meta.id, apiKey)
        val list = listKeys(context).toMutableList()
        list.add(meta)
        context.keyDataStore.edit {
            it[KEY_META_LIST] = AppJson.encodeToString(list)
            it[KEY_CURRENT_ID] = meta.id
        }
        return meta
    }

    suspend fun getCurrentKeyId(context: Context): String {
        return try {
            context.keyDataStore.data.first()[KEY_CURRENT_ID] ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun setCurrentKeyId(context: Context, id: String) {
        context.keyDataStore.edit { it[KEY_CURRENT_ID] = id }
    }

    suspend fun getKey(context: Context, id: String): String = ApiKeyStorage.load(context, id)

    suspend fun getCurrentKey(context: Context): String {
        val id = getCurrentKeyId(context)
        return if (id.isEmpty()) "" else ApiKeyStorage.load(context, id)
    }

    suspend fun findMeta(context: Context, id: String): KeyMeta? {
        return listKeys(context).firstOrNull { it.id == id }
    }

    suspend fun deleteKey(context: Context, id: String) {
        ApiKeyStorage.remove(context, id)
        val list = listKeys(context).filter { it.id != id }
        context.keyDataStore.edit { prefs ->
            prefs[KEY_META_LIST] = AppJson.encodeToString(list)
            val currentId = prefs[KEY_CURRENT_ID] ?: ""
            if (currentId == id) {
                prefs[KEY_CURRENT_ID] = list.firstOrNull()?.id ?: ""
            }
        }
    }

    suspend fun migrateLegacy(context: Context) {
        val legacy = ApiKeyStorage.loadLegacy(context)
        if (legacy.isEmpty()) return
        if (listKeys(context).isNotEmpty()) return
        addKey(context, legacy, "已导入 Key")
        ApiKeyStorage.removeLegacy(context)
    }
}
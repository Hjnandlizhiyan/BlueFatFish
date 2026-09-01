package com.bigfatfish.release.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bigfatfish.release.data.AppJson
import com.bigfatfish.release.data.model.BalanceSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

object BalanceHistoryStore {
    private val KEY_SNAPSHOTS = stringPreferencesKey("snapshots")
    private const val MAX_SNAPSHOTS = 100

    suspend fun list(context: Context): List<BalanceSnapshot> {
        return try {
            val raw = context.balanceHistoryDataStore.data.first()[KEY_SNAPSHOTS] ?: "[]"
            AppJson.decodeFromString<List<BalanceSnapshot>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 追加一条快照。
     * 去重：与最近一条余额相同且间隔 < 5 秒时跳过。
     */
    suspend fun add(context: Context, snapshot: BalanceSnapshot) {
        val list = list(context).toMutableList()
        if (list.isNotEmpty()) {
            val last = list.last()
            if (last.totalBalance == snapshot.totalBalance && (snapshot.timestamp - last.timestamp) < 5000) {
                return
            }
        }
        list.add(snapshot)
        val trimmed = if (list.size > MAX_SNAPSHOTS) list.subList(list.size - MAX_SNAPSHOTS, list.size) else list
        context.balanceHistoryDataStore.edit { it[KEY_SNAPSHOTS] = AppJson.encodeToString(trimmed) }
    }

    /**
     * 按 Key 分组统计余额变化（最新余额 - 首次余额）。
     * 只统计带 keyId 的快照，旧数据（无 keyId）跳过。
     */
    suspend fun consumptionByKey(context: Context): Map<String, Double> {
        val list = list(context)
        val map = mutableMapOf<String, Pair<Double, Double>>()
        for (s in list) {
            if (s.keyId.isEmpty()) continue
            val v = s.totalBalance.toDoubleOrNull() ?: continue
            val entry = map[s.keyId]
            if (entry == null) map[s.keyId] = v to v
            else map[s.keyId] = entry.first to v
        }
        return map.mapValues { it.value.second - it.value.first }
    }

    suspend fun clear(context: Context) {
        context.balanceHistoryDataStore.edit { it[KEY_SNAPSHOTS] = "[]" }
    }
}
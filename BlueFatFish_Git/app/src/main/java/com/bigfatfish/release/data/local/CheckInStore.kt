package com.bigfatfish.release.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bigfatfish.release.data.AppJson
import com.bigfatfish.release.data.model.CheckInState
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CheckInStore {
    private val KEY_CHECK_IN = stringPreferencesKey("check_in_state")

    suspend fun get(context: Context): CheckInState {
        return try {
            val raw = context.settingsDataStore.data.first()[KEY_CHECK_IN] ?: ""
            if (raw.isEmpty()) CheckInState()
            else AppJson.decodeFromString<CheckInState>(raw)
        } catch (e: Exception) {
            CheckInState()
        }
    }

    /** 当天首次成功查询余额时自动打卡一次，返回最新状态。 */
    suspend fun checkIn(context: Context): CheckInState {
        val state = get(context)
        val today = todayStr()
        if (state.lastDate == today) return state
        val newStreak = if (state.lastDate == yesterdayStr()) state.streak + 1 else 1
        val newState = CheckInState(today, newStreak, state.total + 1)
        context.settingsDataStore.edit { it[KEY_CHECK_IN] = AppJson.encodeToString(newState) }
        return newState
    }

    fun title(streak: Int): String = when {
        streak >= 30 -> "大肥鱼传奇"
        streak >= 14 -> "大肥鱼大师"
        streak >= 7 -> "大肥鱼勇士"
        streak >= 3 -> "肥鱼学徒"
        streak >= 1 -> "小鱼苗"
        else -> "未打卡"
    }

    private fun todayStr(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun yesterdayStr(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
}
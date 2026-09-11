package com.bigfatfish.release.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bigfatfish.release.data.AppJson
import com.bigfatfish.release.data.model.RemotePricing
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/** 缓存最近一次从官网成功获取的定价与时段规则 */
object PricingStore {
    private val KEY_PRICING = stringPreferencesKey("remote_pricing")

    suspend fun load(context: Context): RemotePricing? {
        return try {
            val raw = context.pricingDataStore.data.first()[KEY_PRICING] ?: ""
            if (raw.isEmpty()) null else AppJson.decodeFromString<RemotePricing>(raw)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun save(context: Context, pricing: RemotePricing) {
        try {
            context.pricingDataStore.edit { it[KEY_PRICING] = AppJson.encodeToString(pricing) }
        } catch (e: Exception) {
        }
    }
}
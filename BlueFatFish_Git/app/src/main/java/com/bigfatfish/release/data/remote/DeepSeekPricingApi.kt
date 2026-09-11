package com.bigfatfish.release.data.remote

import com.bigfatfish.release.data.model.RemotePricing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object DeepSeekPricingApi {
    const val PRICING_URL = "https://api-docs.deepseek.com/zh-cn/quick_start/pricing"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /** 拉取并解析官网定价页；失败时抛出带用户可读信息的异常，由调用方回退内置数据 */
    suspend fun fetch(): RemotePricing = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(PRICING_URL)
            .header("Accept", "text/html")
            .get()
            .build()
        val html = try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                response.body?.string() ?: ""
            }
        } catch (e: IOException) {
            throw Exception("官网获取失败：用户未联网")
        }
        PricingHtmlParser.parse(html) ?: throw Exception("官网页面解析失败")
    }
}
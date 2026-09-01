package com.bigfatfish.release.data.remote

import com.bigfatfish.release.data.AppJson
import com.bigfatfish.release.data.model.BalanceResponse
import com.bigfatfish.release.data.model.ChatMessage
import com.bigfatfish.release.data.model.ChatRequestBody
import com.bigfatfish.release.data.model.ChatResponse
import com.bigfatfish.release.data.model.ChatResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object DeepSeekApi {
    private const val BASE_URL = "https://api.deepseek.com"

    private val baseClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /** 查询余额 */
    suspend fun queryBalance(apiKey: String): BalanceResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/user/balance")
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .get()
            .build()
        try {
            baseClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                when (response.code) {
                    401 -> throw Exception("密钥不存在或已删除")
                    200 -> AppJson.decodeFromString<BalanceResponse>(body)
                    else -> throw Exception("HTTP ${response.code}: $body")
                }
            }
        } catch (e: IOException) {
            throw Exception("余额查询失败：用户未联网")
        }
    }

    /** AI 聊天（非流式），model 支持 deepseek-chat / deepseek-reasoner */
    suspend fun chat(
        apiKey: String,
        messages: List<ChatMessage>,
        model: String,
        timeoutSeconds: Int = 60
    ): ChatResult = withContext(Dispatchers.IO) {
        val client = baseClient.newBuilder()
            .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .build()

        val cleanMessages = messages.map { ChatMessage(role = it.role, content = it.content) }
        val req = ChatRequestBody(model = model, messages = cleanMessages)
        val jsonBody = AppJson.encodeToString(req)

        val request = Request.Builder()
            .url("$BASE_URL/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        try {
            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string() ?: ""
                when (response.code) {
                    401 -> throw Exception("密钥无效")
                    200 -> {
                        val data = AppJson.decodeFromString<ChatResponse>(respBody)
                        val msg = data.choices.firstOrNull()?.message
                        if (msg != null) {
                            ChatResult(
                                content = msg.content,
                                reasoning_content = msg.reasoning_content ?: ""
                            )
                        } else {
                            ChatResult("", "")
                        }
                    }
                    else -> throw Exception("HTTP ${response.code}: $respBody")
                }
            }
        } catch (e: IOException) {
            throw Exception("对话失败：用户未联网")
        }
    }
}
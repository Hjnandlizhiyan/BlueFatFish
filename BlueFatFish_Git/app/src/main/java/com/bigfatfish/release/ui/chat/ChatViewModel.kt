package com.bigfatfish.release.ui.chat

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bigfatfish.release.data.AppJson
import com.bigfatfish.release.data.local.ChatHistoryStore
import com.bigfatfish.release.data.local.ChatSettingsStore
import com.bigfatfish.release.data.model.ChatMessage
import com.bigfatfish.release.data.model.ChatOpenRequest
import com.bigfatfish.release.data.model.ChatSession
import com.bigfatfish.release.data.model.ChatSettings
import com.bigfatfish.release.data.remote.DeepSeekApi
import com.bigfatfish.release.navigation.NavigationBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString

data class ChatUiState(
    val apiKeyInput: String = "",
    val keyConfirmed: Boolean = false,
    val confirmedKey: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val deepThink: Boolean = false,
    val expandedMsgIndex: Int = -1,
    val contextLimit: Int = 20,
    val historyCount: Int = 0,
    val timeoutSeconds: Int = 60,
    val defaultModel: String = "deepseek-chat",
    val deepThinkDefault: Boolean = false,
    val isLoading: Boolean = false,
    val showSettingsDialog: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication<Application>().applicationContext

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentSessionId: String = System.currentTimeMillis().toString()

    init {
        viewModelScope.launch {
            val s = ChatSettingsStore.get(context)
            _uiState.update {
                it.copy(
                    contextLimit = s.contextLimit,
                    historyCount = s.historyCount,
                    timeoutSeconds = s.timeoutSeconds,
                    defaultModel = s.defaultModel,
                    deepThinkDefault = s.deepThinkDefault,
                    deepThink = s.deepThinkDefault
                )
            }
            currentSessionId = System.currentTimeMillis().toString()
        }
        viewModelScope.launch {
            NavigationBus.chatOpenRequest.collect { raw ->
                if (raw.isNotEmpty()) {
                    NavigationBus.chatOpenRequest.value = ""
                    handleOpenCmd(raw)
                }
            }
        }
    }

    fun updateApiKeyInput(value: String) = _uiState.update { it.copy(apiKeyInput = value) }
    fun updateInputText(value: String) = _uiState.update { it.copy(inputText = value) }
    fun setDeepThink(on: Boolean) = _uiState.update { it.copy(deepThink = on) }

    fun confirmKey() {
        val key = _uiState.value.apiKeyInput.trim()
        if (key.isEmpty()) {
            toast("请先输入 API Key")
            return
        }
        _uiState.update { it.copy(confirmedKey = key, keyConfirmed = true) }
        toast("密钥已确认，可以开始聊天")
    }

    fun resetKey() {
        _uiState.update { it.copy(keyConfirmed = false, confirmedKey = "", apiKeyInput = "") }
    }

    fun newChat() {
        if (_uiState.value.messages.isEmpty()) {
            toast("已经是新对话")
            return
        }
        _uiState.update { it.copy(messages = emptyList(), expandedMsgIndex = -1) }
        currentSessionId = System.currentTimeMillis().toString()
        toast("已开启新对话，历史对话可在「历史」中查看")
    }

    fun doClearChat() {
        viewModelScope.launch {
            val id = currentSessionId
            _uiState.update { it.copy(messages = emptyList(), expandedMsgIndex = -1) }
            removeSessionFromHistory(id)
            currentSessionId = System.currentTimeMillis().toString()
            toast("已清除对话记录")
        }
    }

    fun saveSettings() {
        val s = _uiState.value
        val settings = ChatSettings(
            contextLimit = s.contextLimit,
            historyCount = s.historyCount,
            timeoutSeconds = s.timeoutSeconds,
            defaultModel = s.defaultModel,
            deepThinkDefault = s.deepThinkDefault
        )
        viewModelScope.launch {
            ChatSettingsStore.save(context, settings)
            toast("设置已保存")
        }
    }

    fun updateContextLimit(value: Int) = _uiState.update { it.copy(contextLimit = value) }
    fun updateHistoryCount(value: Int) = _uiState.update { it.copy(historyCount = value) }
    fun updateTimeoutSeconds(value: Int) = _uiState.update { it.copy(timeoutSeconds = value) }

    fun setDefaultModel(model: String) = _uiState.update {
        it.copy(defaultModel = model, deepThinkDefault = model == "deepseek-reasoner")
    }

    fun setDeepThinkDefault(on: Boolean) = _uiState.update {
        it.copy(deepThinkDefault = on, defaultModel = if (on) "deepseek-reasoner" else "deepseek-chat")
    }

    fun openSettingsDialog() = _uiState.update { it.copy(showSettingsDialog = true) }
    fun closeSettingsDialog() = _uiState.update { it.copy(showSettingsDialog = false) }

    fun toggleReasoning(index: Int) = _uiState.update {
        it.copy(expandedMsgIndex = if (it.expandedMsgIndex == index) -1 else index)
    }

    fun send() {
        val state = _uiState.value
        if (!state.keyConfirmed) {
            toast("请先确认 API Key")
            return
        }
        val key = state.confirmedKey
        val text = state.inputText.trim()
        if (text.isEmpty()) {
            toast("请输入消息")
            return
        }
        if (state.isLoading) return

        val userMsg = ChatMessage(id = genMsgId(), role = "user", content = text)
        _uiState.update { it.copy(messages = it.messages + userMsg, inputText = "", isLoading = true) }

        viewModelScope.launch {
            var sendList = _uiState.value.messages
            if (state.contextLimit > 0 && sendList.size > state.contextLimit) {
                sendList = sendList.takeLast(state.contextLimit)
            }
            val model = if (state.deepThink) "deepseek-reasoner" else "deepseek-chat"
            try {
                val result = DeepSeekApi.chat(key, sendList, model, state.timeoutSeconds)
                val assistantMsg = ChatMessage(
                    id = genMsgId(),
                    role = "assistant",
                    content = result.content,
                    reasoning_content = result.reasoning_content
                )
                _uiState.update { it.copy(messages = it.messages + assistantMsg) }
            } catch (e: Exception) {
                val errMsg = ChatMessage(
                    id = genMsgId(),
                    role = "assistant",
                    content = "请求失败：" + (e.message ?: "")
                )
                _uiState.update { it.copy(messages = it.messages + errMsg) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
                persistCurrentSession()
            }
        }
    }

    private suspend fun persistCurrentSession() {
        val msgs = _uiState.value.messages
        val sessionId = currentSessionId
        if (msgs.isEmpty()) return
        val sessions = ChatHistoryStore.list(context)
        var title = ""
        for (s in sessions) {
            if (s.id == sessionId && s.title.isNotEmpty()) {
                title = s.title
                break
            }
        }
        if (title.isEmpty()) {
            for (m in msgs) {
                if (m.role == "user") {
                    title = m.content
                    break
                }
            }
            if (title.length > 20) {
                title = title.substring(0, 20) + "..."
            }
        }
        val session = ChatSession(
            id = sessionId,
            title = title,
            messages = msgs,
            updatedAt = System.currentTimeMillis()
        )
        val filtered = sessions.filter { it.id != sessionId }.toMutableList()
        filtered.add(0, session)
        var toSave: List<ChatSession> = filtered
        if (_uiState.value.historyCount > 0 && toSave.size > _uiState.value.historyCount) {
            toSave = toSave.take(_uiState.value.historyCount)
        }
        ChatHistoryStore.save(context, toSave)
    }

    private suspend fun removeSessionFromHistory(id: String) {
        val sessions = ChatHistoryStore.list(context)
        ChatHistoryStore.save(context, sessions.filter { it.id != id })
    }

    private suspend fun loadSession(id: String) {
        val sessions = ChatHistoryStore.list(context)
        for (s in sessions) {
            if (s.id == id) {
                val msgs = s.messages.mapIndexed { i, m ->
                    ChatMessage(
                        id = if (m.id != null && m.id.isNotEmpty()) m.id else (s.id + "_" + i.toString()),
                        role = m.role,
                        content = m.content,
                        reasoning_content = m.reasoning_content
                    )
                }
                _uiState.update { it.copy(messages = msgs, expandedMsgIndex = -1) }
                currentSessionId = s.id
                break
            }
        }
    }

    private fun handleOpenCmd(raw: String) {
        val req = try {
            AppJson.decodeFromString<ChatOpenRequest>(raw)
        } catch (e: Exception) {
            return
        }
        if (req.action == "new") {
            newChat()
        } else {
            viewModelScope.launch { loadSession(req.id) }
        }
    }

    private fun genMsgId(): String {
        return System.currentTimeMillis().toString() + "_" +
            ((Math.random() * 1000000).toInt()).toString()
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
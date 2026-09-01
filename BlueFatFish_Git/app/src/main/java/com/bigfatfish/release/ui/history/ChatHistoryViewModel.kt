package com.bigfatfish.release.ui.history

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bigfatfish.release.data.local.ChatHistoryStore
import com.bigfatfish.release.data.model.ChatSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication<Application>().applicationContext

    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { _sessions.value = ChatHistoryStore.list(context) }
    }

    fun rename(id: String, newTitle: String) {
        viewModelScope.launch {
            val title = newTitle.trim()
            if (title.isEmpty()) {
                toast("名称不能为空")
                return@launch
            }
            val list = ChatHistoryStore.list(context).map { if (it.id == id) it.copy(title = title) else it }
            ChatHistoryStore.save(context, list)
            _sessions.value = ChatHistoryStore.list(context)
            toast("已重命名")
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            val list = ChatHistoryStore.list(context).filter { it.id != id }
            ChatHistoryStore.save(context, list)
            _sessions.value = ChatHistoryStore.list(context)
            toast("已删除")
        }
    }

    private fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
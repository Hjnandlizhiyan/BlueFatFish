package com.bigfatfish.release.ui.balance

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bigfatfish.release.data.local.BalanceHistoryStore
import com.bigfatfish.release.data.model.BalanceSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BalanceHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication<Application>().applicationContext

    private val _snapshots = MutableStateFlow<List<BalanceSnapshot>>(emptyList())
    val snapshots: StateFlow<List<BalanceSnapshot>> = _snapshots.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { _snapshots.value = BalanceHistoryStore.list(context) }
    }

    fun clear() {
        viewModelScope.launch {
            BalanceHistoryStore.clear(context)
            _snapshots.value = emptyList()
            toast("历史记录已清除")
        }
    }

    private fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
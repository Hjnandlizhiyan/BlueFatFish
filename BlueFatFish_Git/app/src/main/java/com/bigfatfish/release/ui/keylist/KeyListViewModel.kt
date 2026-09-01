package com.bigfatfish.release.ui.keylist

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bigfatfish.release.data.local.BalanceHistoryStore
import com.bigfatfish.release.data.local.KeyManager
import com.bigfatfish.release.data.model.KeyMeta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KeyListViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication<Application>().applicationContext

    private val _keys = MutableStateFlow<List<KeyMeta>>(emptyList())
    val keys: StateFlow<List<KeyMeta>> = _keys.asStateFlow()

    private val _currentKeyId = MutableStateFlow("")
    val currentKeyId: StateFlow<String> = _currentKeyId.asStateFlow()

    private val _consumption = MutableStateFlow<Map<String, Double>>(emptyMap())
    val consumption: StateFlow<Map<String, Double>> = _consumption.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _keys.value = KeyManager.listKeys(context)
            _currentKeyId.value = KeyManager.getCurrentKeyId(context)
            _consumption.value = BalanceHistoryStore.consumptionByKey(context)
        }
    }

    fun switchKey(meta: KeyMeta) {
        viewModelScope.launch {
            KeyManager.setCurrentKeyId(context, meta.id)
            refresh()
            toast("已切换至：" + meta.note)
        }
    }

    fun delete(meta: KeyMeta) {
        viewModelScope.launch {
            KeyManager.deleteKey(context, meta.id)
            refresh()
            toast("已删除")
        }
    }

    private fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
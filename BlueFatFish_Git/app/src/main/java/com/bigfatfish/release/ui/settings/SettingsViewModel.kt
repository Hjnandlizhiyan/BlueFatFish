package com.bigfatfish.release.ui.settings

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bigfatfish.release.data.local.SettingsStore
import com.bigfatfish.release.data.model.AppSettings
import com.bigfatfish.release.ui.theme.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication<Application>().applicationContext

    private val _settings = MutableStateFlow(SettingsStore.defaultSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val s = SettingsStore.get(context)
            _settings.value = s
            ThemeManager.themeMode.value = s.themeMode
            ThemeManager.skin.value = s.skin
        }
    }

    fun updateThemeMode(mode: String) {
        _settings.value = _settings.value.copy(themeMode = mode)
        ThemeManager.themeMode.value = mode
    }

    fun updateSkin(value: String) {
        _settings.value = _settings.value.copy(skin = value)
        ThemeManager.skin.value = value
    }

    fun updateThreshold(value: String) {
        _settings.value = _settings.value.copy(threshold = value)
    }

    fun updateAlertEnabled(value: Boolean) {
        _settings.value = _settings.value.copy(alertEnabled = value)
    }

    fun updateMaskKey(value: Boolean) {
        _settings.value = _settings.value.copy(maskKey = value)
    }

    fun updateShowIcon(value: Boolean) {
        _settings.value = _settings.value.copy(showIcon = value)
    }

    fun updateShowGroupBuy(value: Boolean) {
        _settings.value = _settings.value.copy(showGroupBuy = value)
    }

    fun save() {
        viewModelScope.launch {
            SettingsStore.save(context, _settings.value)
            ThemeManager.themeMode.value = _settings.value.themeMode
            ThemeManager.skin.value = _settings.value.skin
            toast("设置已保存")
        }
    }

    private fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
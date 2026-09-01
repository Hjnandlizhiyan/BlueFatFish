package com.bigfatfish.release.ui.home

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bigfatfish.release.data.local.BalanceHistoryStore
import com.bigfatfish.release.data.local.CheckInStore
import com.bigfatfish.release.data.local.KeyManager
import com.bigfatfish.release.data.local.SettingsStore
import com.bigfatfish.release.data.model.AppSettings
import com.bigfatfish.release.data.model.BalanceInfo
import com.bigfatfish.release.data.model.BalanceSnapshot
import com.bigfatfish.release.data.model.CheckInState
import com.bigfatfish.release.data.notification.NotificationHelper
import com.bigfatfish.release.data.remote.DeepSeekApi
import com.bigfatfish.release.ui.theme.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardUiState(
    val apiKeyInput: String = "",
    val noteInput: String = "",
    val hasBound: Boolean = false,
    val currentNote: String = "",
    val currentKeyPlain: String = "",
    val showPlainKey: Boolean = false,
    val totalBalance: String = "--",
    val balances: List<BalanceInfo> = emptyList(),
    val currency: String = "CNY",
    val isLoading: Boolean = false,
    val errorDetail: String = "",
    val settings: AppSettings = AppSettings("system", "", true, true, false, false),
    val iconIndex: Int = 0,
    val currentIndex: Int = 0,
    val showNetworkNotice: Boolean = false,
    val checkIn: CheckInState = CheckInState()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication<Application>().applicationContext

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { initData() }
    }

    private suspend fun initData() {
        val settings = withContext(Dispatchers.IO) {
            KeyManager.migrateLegacy(context)
            SettingsStore.get(context)
        }
        ThemeManager.themeMode.value = settings.themeMode
        ThemeManager.skin.value = settings.skin
        val checkIn = withContext(Dispatchers.IO) { CheckInStore.get(context) }
        _uiState.update { it.copy(settings = settings, showPlainKey = !settings.maskKey, checkIn = checkIn) }
        reloadCurrentKey()
        val noticeShown = withContext(Dispatchers.IO) {
            SettingsStore.hasShownNetworkNotice(context)
        }
        if (!noticeShown) {
            withContext(Dispatchers.IO) {
                SettingsStore.markNetworkNoticeShown(context)
            }
            delay(400)
            _uiState.update { it.copy(showNetworkNotice = true) }
        }
    }

    /** 对应鸿蒙 onPageShow：从列表/设置页返回时刷新 */
    fun onResume() {
        viewModelScope.launch {
            val settings = SettingsStore.get(context)
            ThemeManager.themeMode.value = settings.themeMode
            ThemeManager.skin.value = settings.skin
            _uiState.update { it.copy(settings = settings, showPlainKey = !settings.maskKey) }
            reloadCurrentKey()
        }
    }

    private suspend fun reloadCurrentKey() {
        val key = KeyManager.getCurrentKey(context)
        if (key.isEmpty()) {
            _uiState.update {
                it.copy(
                    hasBound = false,
                    currentKeyPlain = "",
                    currentNote = "",
                    totalBalance = "--",
                    balances = emptyList()
                )
            }
            return
        }
        val id = KeyManager.getCurrentKeyId(context)
        val meta = KeyManager.findMeta(context, id)
        _uiState.update {
            it.copy(
                hasBound = true,
                currentKeyPlain = key,
                currentNote = meta?.note ?: ""
            )
        }
        loadBalance()
    }

    fun updateApiKeyInput(value: String) = _uiState.update { it.copy(apiKeyInput = value) }
    fun updateNoteInput(value: String) = _uiState.update { it.copy(noteInput = value) }

    fun saveKey() {
        val state = _uiState.value
        val key = state.apiKeyInput.trim()
        if (key.isEmpty()) {
            toast("请输入 API Key")
            return
        }
        if (!key.startsWith("sk-") || key.length < 20) {
            toast("Key 格式不正确")
            return
        }
        viewModelScope.launch {
            try {
                var note = state.noteInput.trim()
                if (note.isEmpty()) {
                    val list = KeyManager.listKeys(context)
                    note = "Key " + (list.size + 1)
                }
                KeyManager.addKey(context, key, note)
                _uiState.update { it.copy(apiKeyInput = "", noteInput = "") }
                toast("API Key 已保存")
                reloadCurrentKey()
            } catch (e: Exception) {
                toast("保存失败")
            }
        }
    }

    fun refreshBalance() {
        if (!_uiState.value.hasBound) {
            toast("请先绑定 API Key")
            return
        }
        viewModelScope.launch { loadBalance() }
    }

    private suspend fun loadBalance() {
        val state = _uiState.value
        if (!state.hasBound || state.currentKeyPlain.isEmpty()) return
        _uiState.update { it.copy(isLoading = true) }
        try {
            val resp = DeepSeekApi.queryBalance(state.currentKeyPlain)
            if (!resp.is_available) {
                _uiState.update { it.copy(isLoading = false, totalBalance = "--", balances = emptyList()) }
                toast("账户不可用")
                return
            }
            if (resp.balance_infos.isEmpty()) {
                _uiState.update {
                    it.copy(isLoading = false, totalBalance = "0.00", balances = emptyList(), currency = "CNY")
                }
                return
            }
            val balances = resp.balance_infos
            var primary = balances[0]
            for (info in balances) {
                if (info.currency == "CNY") {
                    primary = info
                    break
                }
            }
            val snapshot = BalanceSnapshot(
                timestamp = System.currentTimeMillis(),
                totalBalance = primary.total_balance,
                currency = primary.currency,
                keyId = KeyManager.getCurrentKeyId(context)
            )
            BalanceHistoryStore.add(context, snapshot)
            val checkIn = CheckInStore.checkIn(context)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    totalBalance = primary.total_balance,
                    balances = balances,
                    currency = primary.currency,
                    checkIn = checkIn
                )
            }
            checkLowBalance(primary.total_balance)
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    totalBalance = "--",
                    balances = emptyList(),
                    errorDetail = buildErrorDetail("刷新余额", "GET /user/balance", state.currentKeyPlain, e.message ?: "")
                )
            }
            toast(e.message ?: "余额查询失败")
        }
    }

    private suspend fun checkLowBalance(totalBalance: String) {
        val s = SettingsStore.get(context)
        if (!s.alertEnabled || s.threshold.isEmpty()) return
        val bal = totalBalance.toDoubleOrNull() ?: return
        val th = s.threshold.toDoubleOrNull() ?: return
        if (bal < th) {
            val ok = NotificationHelper.publishLowBalance(context, totalBalance, s.threshold)
            if (!ok) {
                toast("余额 $totalBalance 已低于阈值 ${s.threshold}，请及时充值")
            }
        }
    }

    fun copyKey() {
        val state = _uiState.value
        if (!state.hasBound || state.currentKeyPlain.isEmpty()) {
            toast("请先绑定 API Key")
            return
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("api_key", state.currentKeyPlain))
        toast("已复制")
    }

    fun toggleShowKey() = _uiState.update { it.copy(showPlainKey = !it.showPlainKey) }
    fun clearError() = _uiState.update { it.copy(errorDetail = "") }
    fun setCurrentIndex(index: Int) = _uiState.update { it.copy(currentIndex = index) }
    fun toggleIcon() = _uiState.update { it.copy(iconIndex = if (it.iconIndex == 0) 1 else 0) }
    fun dismissNetworkNotice() = _uiState.update { it.copy(showNetworkNotice = false) }

    fun maskKey(key: String): String {
        return if (key.length <= 10) key.substring(0, 3) + "****"
        else key.substring(0, 5) + "****" + key.substring(key.length - 4)
    }

    fun displayKey(): String {
        val state = _uiState.value
        if (!state.hasBound) return ""
        return if (state.showPlainKey) state.currentKeyPlain else maskKey(state.currentKeyPlain)
    }

    private fun buildErrorDetail(action: String, endpoint: String, apiKey: String, error: String): String {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        return "操作：$action\n时间：$time\n接口：$endpoint\nKey：${maskKey(apiKey)}\n错误：$error"
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
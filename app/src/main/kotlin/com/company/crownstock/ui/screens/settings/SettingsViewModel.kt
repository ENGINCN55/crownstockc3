package com.company.crownstock.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.AppSetting
import com.company.crownstock.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #16 (Bölüm 13-14) — SettingsScreen:
 * "Düşük stok eşiği gibi genel ayarların yönetimi"
 * Bölüm 4.3.6'daki örnek ayar anahtarı: "global_low_stock_threshold".
 *
 * NOT: Bu global ayarın, item bazındaki minStockThreshold (Bölüm 4.3.1) ile
 * ilişkisi dokümanda tanımlanmamış (örn. yeni ürün eklenirken varsayılan değer
 * mi, yoksa genel bir uyarı eşiği mi?). Bu belirsizlik nedeniyle burada yalnızca
 * ayarın okunup/yazılması sağlandı; iş mantığına (örn. getLowStockItems'a) dahil
 * edilmedi.
 */
private const val KEY_GLOBAL_LOW_STOCK_THRESHOLD = "global_low_stock_threshold"

data class SettingsUiState(
    val isLoading: Boolean = true,
    val globalLowStockThreshold: String = "",
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val setting = settingsRepository.getSetting(KEY_GLOBAL_LOW_STOCK_THRESHOLD)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    globalLowStockThreshold = setting?.value?.toString() ?: ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun onThresholdChanged(value: String) {
        _uiState.value = _uiState.value.copy(globalLowStockThreshold = value, isSaved = false)
    }

    fun save() {
        viewModelScope.launch {
            val value = _uiState.value.globalLowStockThreshold.toDoubleOrNull()
            if (value == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "Geçerli bir sayı girin")
                return@launch
            }
            try {
                settingsRepository.setSetting(AppSetting(settingKey = KEY_GLOBAL_LOW_STOCK_THRESHOLD, value = value))
                _uiState.value = _uiState.value.copy(isSaved = true, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}

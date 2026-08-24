package com.company.crownstock.ui.screens.capacity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #17 (Bölüm 13-14 / 35.6) — CapacityAnalysisScreen:
 * "Kullanıcının sipariş miktarı girip 'Kapasite Kontrol Et' butonuna bastığı
 * giriş ekranı; üretim başlatmaz, yalnızca analiz tetikler."
 * "Nihai ürün seçimi (varsayılan: Crown, ama items koleksiyonundaki diğer
 * NIHAI_URUN kayıtları da seçilebilir)."
 */
data class CapacityAnalysisUiState(
    val nihaiUrunler: List<Item> = emptyList(),
    val selectedItem: Item? = null,
    val quantity: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class CapacityAnalysisViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CapacityAnalysisUiState())
    val uiState: StateFlow<CapacityAnalysisUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val items = itemRepository.getItemsByType(ItemType.NIHAI_URUN)
            // Varsayılan: "Crown" (dokümandaki örnek nihai ürün) varsa o seçili gelir.
            val default = items.firstOrNull { it.name.equals("Crown", ignoreCase = true) } ?: items.firstOrNull()
            _uiState.value = _uiState.value.copy(nihaiUrunler = items, selectedItem = default)
        }
    }

    fun onItemSelected(item: Item) { _uiState.value = _uiState.value.copy(selectedItem = item) }
    fun onQuantityChanged(value: String) { _uiState.value = _uiState.value.copy(quantity = value) }

    /** Doğrulama sonrası true dönerse, çağıran taraf CapacityAnalysisResultScreen'e yönlendirir. */
    fun validateAndProceed(): Pair<String, Double>? {
        val item = _uiState.value.selectedItem
        val qty = _uiState.value.quantity.toDoubleOrNull()
        if (item == null || qty == null || qty <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Ürün ve geçerli bir sipariş miktarı girilmelidir")
            return null
        }
        return item.itemId to qty
    }
}

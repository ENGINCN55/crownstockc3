package com.company.crownstock.ui.screens.shortage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.data.repository.CalculationRepository
import com.company.crownstock.data.repository.ItemRepository
import com.company.crownstock.domain.model.ShortageDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #12 (Bölüm 13-14) — ShortageOverviewScreen (tek seviye):
 * "Belirli bir üretim hedefi için doğrudan eksik bileşenlerin listesi" — Bölüm 22.
 *
 * Bölüm 31 (Uygulama Ekran Akış Diyagramı): DashboardScreen'den DOĞRUDAN erişilen
 * bir uçtur — bu yüzden ekran, önceden seçili bir ürün/adet olmadan da (Dashboard'dan
 * geldiğinde) çalışabilmelidir: kullanıcı ürünü ve adedi ekranın içinde seçer
 * (CapacityAnalysisScreen/MaxProducibleCalculatorScreen ile aynı desen).
 * Önceden bir itemId/quantity verilmişse (örn. CapacityAnalysisScreen'den), otomatik hesaplanır.
 */
data class ShortageOverviewUiState(
    val isLoading: Boolean = false,
    val nihaiUrunler: List<Item> = emptyList(),
    val selectedItem: Item? = null,
    val requestedQuantity: String = "",
    val shortages: List<ShortageDetail> = emptyList(),
    val hasCalculated: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ShortageOverviewViewModel @Inject constructor(
    private val calculationRepository: CalculationRepository,
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val preselectedItemId: String? = savedStateHandle["itemId"]
    private val preselectedQuantity: Double? = (savedStateHandle.get<String>("quantity"))?.toDoubleOrNull()

    private val _uiState = MutableStateFlow(ShortageOverviewUiState())
    val uiState: StateFlow<ShortageOverviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val items = itemRepository.getItemsByType(ItemType.NIHAI_URUN)
            val preselected = preselectedItemId?.let { id -> items.firstOrNull { it.itemId == id } }
            _uiState.value = _uiState.value.copy(
                nihaiUrunler = items,
                selectedItem = preselected,
                requestedQuantity = preselectedQuantity?.toString() ?: ""
            )
            if (preselected != null && preselectedQuantity != null) {
                calculate(preselected.itemId, preselectedQuantity)
            }
        }
    }

    fun onItemSelected(item: Item) { _uiState.value = _uiState.value.copy(selectedItem = item) }
    fun onQuantityChanged(value: String) { _uiState.value = _uiState.value.copy(requestedQuantity = value) }

    fun calculateClicked() {
        val item = _uiState.value.selectedItem
        val qty = _uiState.value.requestedQuantity.toDoubleOrNull()
        if (item == null || qty == null || qty <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Ürün ve geçerli bir miktar seçilmelidir")
            return
        }
        calculate(item.itemId, qty)
    }

    private fun calculate(targetItemId: String, requestedQuantity: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Bölüm 22 — tek seviye, recursive değil.
                val shortages = calculationRepository.calculateDirectShortage(targetItemId, requestedQuantity)
                _uiState.value = _uiState.value.copy(isLoading = false, shortages = shortages, hasCalculated = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}

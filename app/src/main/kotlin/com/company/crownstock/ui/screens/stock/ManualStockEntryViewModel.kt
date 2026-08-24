package com.company.crownstock.ui.screens.stock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.data.model.MovementType
import com.company.crownstock.data.repository.ItemRepository
import com.company.crownstock.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #7 (Bölüm 13-14) — ManualStockEntryScreen:
 * "Elle stok girişi/çıkışı (ürün seçimi, miktar, neden)"
 * İş kuralı Bölüm 19 — StockRepository.manualStockEntry içinde uygulanır.
 */
data class ManualStockEntryUiState(
    val availableItems: List<Item> = emptyList(),
    val selectedItem: Item? = null,
    val movementType: MovementType = MovementType.MANUAL_IN,
    val quantity: String = "",
    val reason: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val lowStockWarning: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ManualStockEntryViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val stockRepository: StockRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val preselectedItemId: String? = savedStateHandle["itemId"]

    private val _uiState = MutableStateFlow(ManualStockEntryUiState())
    val uiState: StateFlow<ManualStockEntryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val allItems = ItemType.entries.flatMap { itemRepository.getItemsByType(it) }
            val preselected = preselectedItemId?.let { id -> allItems.firstOrNull { it.itemId == id } }
            _uiState.value = _uiState.value.copy(availableItems = allItems, selectedItem = preselected)
        }
    }

    fun onItemSelected(item: Item) { _uiState.value = _uiState.value.copy(selectedItem = item) }
    fun onMovementTypeChanged(type: MovementType) { _uiState.value = _uiState.value.copy(movementType = type) }
    fun onQuantityChanged(value: String) { _uiState.value = _uiState.value.copy(quantity = value) }
    fun onReasonChanged(value: String) { _uiState.value = _uiState.value.copy(reason = value) }

    fun submit() {
        viewModelScope.launch {
            val state = _uiState.value
            val item = state.selectedItem
            val qty = state.quantity.toDoubleOrNull()
            if (item == null || qty == null || qty <= 0) {
                _uiState.value = state.copy(errorMessage = "Ürün ve geçerli bir miktar seçilmelidir")
                return@launch
            }

            _uiState.value = state.copy(isSaving = true, errorMessage = null)
            try {
                stockRepository.manualStockEntry(
                    itemId = item.itemId,
                    quantity = qty,
                    movementType = state.movementType,
                    reason = state.reason.ifBlank { null },
                    // MVP'de kullanılmaz (Bölüm 4.3.3)
                    performedBy = null
                )
                // Adım 4 (Bölüm 19): yeni stok minStockThreshold altına düştüyse UI'da uyarı gösterilir.
                val newStock = if (state.movementType == MovementType.MANUAL_IN) item.currentStock + qty else item.currentStock - qty
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSaved = true,
                    lowStockWarning = newStock < item.minStockThreshold
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message)
            }
        }
    }
}

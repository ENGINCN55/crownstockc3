package com.company.crownstock.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.data.model.MovementType
import com.company.crownstock.data.model.StockMovement
import com.company.crownstock.data.repository.ItemRepository
import com.company.crownstock.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * Ekran #15 (Bölüm 13-14) — StockMovementHistoryScreen:
 * "Tüm stok hareketlerinin filtrelenebilir (tarih, ürün, tip) geçmiş listesi"
 * Bölüm 25: "filtreleme kriterleri: ürün, tarih aralığı, hareket tipi."
 *
 * Not: "Tüm" hareketler ürün bazında sorgulanabiliyor (StockMovementDataSource,
 * Bölüm 7 — yalnızca itemId bazlı sorgu tanımlı, "tüm ürünler için" bir sorgu
 * yok). Bu yüzden ürün seçimi zorunlu tutuldu; varsayılan olarak ilk ürün seçilir.
 */
data class StockMovementHistoryUiState(
    val isLoading: Boolean = true,
    val availableItems: List<Item> = emptyList(),
    val selectedItem: Item? = null,
    val movementTypeFilter: MovementType? = null,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val movements: List<StockMovement> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class StockMovementHistoryViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val stockRepository: StockRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val preselectedItemId: String? = savedStateHandle["itemId"]

    private val _uiState = MutableStateFlow(StockMovementHistoryUiState())
    val uiState: StateFlow<StockMovementHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val allItems = ItemType.entries.flatMap { itemRepository.getItemsByType(it) }
            val preselected = preselectedItemId?.let { id -> allItems.firstOrNull { it.itemId == id } } ?: allItems.firstOrNull()
            _uiState.value = _uiState.value.copy(availableItems = allItems, selectedItem = preselected)
            loadHistory()
        }
    }

    fun onItemSelected(item: Item) { _uiState.value = _uiState.value.copy(selectedItem = item); loadHistory() }
    fun onMovementTypeFilterChanged(type: MovementType?) { _uiState.value = _uiState.value.copy(movementTypeFilter = type); loadHistory() }
    fun onDateRangeChanged(start: Date?, end: Date?) { _uiState.value = _uiState.value.copy(startDate = start, endDate = end); loadHistory() }

    fun loadHistory() {
        val item = _uiState.value.selectedItem ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val state = _uiState.value
                val movements = stockRepository.getMovementHistory(
                    itemId = item.itemId,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    movementType = state.movementTypeFilter
                )
                _uiState.value = _uiState.value.copy(isLoading = false, movements = movements)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}

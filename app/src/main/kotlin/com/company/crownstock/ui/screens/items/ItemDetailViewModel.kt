package com.company.crownstock.ui.screens.items

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.StockMovement
import com.company.crownstock.data.repository.ItemRepository
import com.company.crownstock.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #3 (Bölüm 13-14) — ItemDetailScreen:
 * "Seçilen ürünün tüm bilgileri, stok geçmişi özeti, BOM'a kısayol"
 */
data class ItemDetailUiState(
    val isLoading: Boolean = true,
    val item: Item? = null,
    val recentMovements: List<StockMovement> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val stockRepository: StockRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle["itemId"])

    private val _uiState = MutableStateFlow(ItemDetailUiState())
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val item = itemRepository.getItemById(itemId)
                // "Stok geçmişi özeti": tam geçmiş StockMovementHistoryScreen'de (Ekran #15);
                // burada yalnızca en güncel hareketler kısaca gösterilir.
                val movements = stockRepository.getMovementHistory(itemId).take(10)
                _uiState.value = ItemDetailUiState(isLoading = false, item = item, recentMovements = movements)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun deactivateItem(onDone: () -> Unit) {
        viewModelScope.launch {
            itemRepository.deactivateItem(itemId)
            onDone()
        }
    }
}

package com.company.crownstock.ui.screens.stock

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
 * Ekran #6 (Bölüm 13-14) — StockOverviewScreen:
 * "Tüm ürünlerin güncel stok tablosu, düşük stok filtreleme"
 */
data class StockOverviewUiState(
    val isLoading: Boolean = true,
    val items: List<Item> = emptyList(),
    val lowStockOnly: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class StockOverviewViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockOverviewUiState())
    val uiState: StateFlow<StockOverviewUiState> = _uiState.asStateFlow()

    init { load() }

    fun toggleLowStockOnly() {
        _uiState.value = _uiState.value.copy(lowStockOnly = !_uiState.value.lowStockOnly)
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val items = if (_uiState.value.lowStockOnly) {
                    itemRepository.getLowStockItems()
                } else {
                    ItemType.entries.flatMap { itemRepository.getItemsByType(it) }
                }
                _uiState.value = _uiState.value.copy(isLoading = false, items = items)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}

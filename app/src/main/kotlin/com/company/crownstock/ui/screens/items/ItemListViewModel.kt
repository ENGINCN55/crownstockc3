package com.company.crownstock.ui.screens.items

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
 * Ekran #2 (Bölüm 13-14) — ItemListScreen:
 * "Ham madde/yarı mamül/nihai ürün listesi; tipe göre filtreleme, arama,
 * stok durumu göstergesi"
 */
data class ItemListUiState(
    val isLoading: Boolean = true,
    val items: List<Item> = emptyList(),
    val selectedType: ItemType? = null, // null = tümü
    val searchQuery: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class ItemListViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemListUiState())
    val uiState: StateFlow<ItemListUiState> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    fun onTypeSelected(type: ItemType?) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        loadItems()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val state = _uiState.value
                val result = when {
                    state.searchQuery.isNotBlank() -> itemRepository.searchItemsByName(state.searchQuery)
                    state.selectedType != null -> itemRepository.getItemsByType(state.selectedType)
                    else -> ItemType.entries.flatMap { itemRepository.getItemsByType(it) }
                }
                _uiState.value = _uiState.value.copy(isLoading = false, items = result)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}

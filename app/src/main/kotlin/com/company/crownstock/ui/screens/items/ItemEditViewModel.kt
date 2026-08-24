package com.company.crownstock.ui.screens.items

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.AuditAction
import com.company.crownstock.data.model.AuditEntityType
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.data.repository.AuditRepository
import com.company.crownstock.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #4 (Bölüm 13-14) — ItemEditScreen:
 * "Yeni ürün ekleme / mevcut ürünü düzenleme (ad, tip, birim, eşik değeri)"
 */
data class ItemEditUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val name: String = "",
    val itemType: ItemType = ItemType.HAM_MADDE,
    val unit: String = "",
    val minStockThreshold: String = "0",
    val description: String = "",
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ItemEditViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val auditRepository: AuditRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: String? = savedStateHandle["itemId"]

    private val _uiState = MutableStateFlow(ItemEditUiState(isEditMode = itemId != null))
    val uiState: StateFlow<ItemEditUiState> = _uiState.asStateFlow()

    init {
        if (itemId != null) loadExisting(itemId)
    }

    private fun loadExisting(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val item = itemRepository.getItemById(id)
            if (item != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    name = item.name,
                    itemType = item.itemType,
                    unit = item.unit,
                    minStockThreshold = item.minStockThreshold.toString(),
                    description = item.description ?: ""
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Ürün bulunamadı")
            }
        }
    }

    fun onNameChanged(value: String) { _uiState.value = _uiState.value.copy(name = value) }
    fun onTypeChanged(value: ItemType) { _uiState.value = _uiState.value.copy(itemType = value) }
    fun onUnitChanged(value: String) { _uiState.value = _uiState.value.copy(unit = value) }
    fun onMinStockThresholdChanged(value: String) { _uiState.value = _uiState.value.copy(minStockThreshold = value) }
    fun onDescriptionChanged(value: String) { _uiState.value = _uiState.value.copy(description = value) }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            val threshold = state.minStockThreshold.toDoubleOrNull()
            if (state.name.isBlank() || state.unit.isBlank() || threshold == null) {
                _uiState.value = state.copy(errorMessage = "Ad, birim ve eşik değeri zorunludur")
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            try {
                if (itemId != null) {
                    val existing = itemRepository.getItemById(itemId)
                    val updated = (existing ?: Item(itemId = itemId)).copy(
                        name = state.name,
                        itemType = state.itemType,
                        unit = state.unit,
                        minStockThreshold = threshold,
                        description = state.description.ifBlank { null }
                    )
                    itemRepository.updateItem(updated)
                    auditRepository.logChange(AuditEntityType.ITEM, itemId, AuditAction.UPDATE)
                } else {
                    val newItem = Item(
                        name = state.name,
                        itemType = state.itemType,
                        unit = state.unit,
                        currentStock = 0.0,
                        minStockThreshold = threshold,
                        description = state.description.ifBlank { null }
                    )
                    val newId = itemRepository.addItem(newItem)
                    auditRepository.logChange(AuditEntityType.ITEM, newId, AuditAction.CREATE)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}

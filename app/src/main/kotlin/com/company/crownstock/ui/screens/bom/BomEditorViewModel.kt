package com.company.crownstock.ui.screens.bom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.AuditAction
import com.company.crownstock.data.model.AuditEntityType
import com.company.crownstock.data.model.BomComponent
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.data.repository.AuditRepository
import com.company.crownstock.data.repository.BomRepository
import com.company.crownstock.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #5 (Bölüm 13-14) — BomEditorScreen:
 * "Seçilen ürün için alt bileşen listesi ve miktarlarının tanımlanması/düzenlenmesi"
 * Doğrulama kuralları BomRepository'de uygulanır (Bölüm 16).
 */
data class BomComponentRow(val component: BomComponent, val childItem: Item?)

data class BomEditorUiState(
    val isLoading: Boolean = true,
    val parentItem: Item? = null,
    val components: List<BomComponentRow> = emptyList(),
    val availableChildItems: List<Item> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class BomEditorViewModel @Inject constructor(
    private val bomRepository: BomRepository,
    private val itemRepository: ItemRepository,
    private val auditRepository: AuditRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val parentItemId: String = checkNotNull(savedStateHandle["itemId"])

    private val _uiState = MutableStateFlow(BomEditorUiState())
    val uiState: StateFlow<BomEditorUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val parent = itemRepository.getItemById(parentItemId)
                val components = bomRepository.getComponentsByParent(parentItemId)
                val rows = components.map { c -> BomComponentRow(c, itemRepository.getItemById(c.childItemId)) }
                // Olası alt bileşenler: kendisi hariç tüm ürünler (Bölüm 16 doğrulaması ekleme anında tekrar yapılır).
                val allItems = ItemType.entries.flatMap { itemRepository.getItemsByType(it) }
                    .filter { it.itemId != parentItemId }
                _uiState.value = BomEditorUiState(
                    isLoading = false,
                    parentItem = parent,
                    components = rows,
                    availableChildItems = allItems
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun addComponent(childItemId: String, quantityPerUnit: Double, unit: String) {
        viewModelScope.launch {
            try {
                val newId = bomRepository.addBomComponent(
                    BomComponent(
                        parentItemId = parentItemId,
                        childItemId = childItemId,
                        quantityPerUnit = quantityPerUnit,
                        unit = unit
                    )
                )
                auditRepository.logChange(AuditEntityType.BOM_COMPONENT, newId, AuditAction.CREATE)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun removeComponent(bomId: String) {
        viewModelScope.launch {
            bomRepository.deactivateBomComponent(bomId)
            auditRepository.logChange(AuditEntityType.BOM_COMPONENT, bomId, AuditAction.DELETE)
            load()
        }
    }
}

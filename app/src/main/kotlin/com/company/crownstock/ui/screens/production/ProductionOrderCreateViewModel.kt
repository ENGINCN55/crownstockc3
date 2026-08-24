package com.company.crownstock.ui.screens.production

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.data.repository.ItemRepository
import com.company.crownstock.data.repository.ProductionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #8 (Bölüm 13-14) — ProductionOrderCreateScreen:
 * "Üretilecek ürün ve adet seçimi; sistem otomatik olarak eksik/darboğaz ön kontrolü yapar"
 * Sekans: Bölüm 29.2 — hesaplaEksik -> ProductionRepository.createDraftOrder
 * (IK-7: DRAFT aşamasında stok düşülmez).
 */
data class ProductionOrderCreateUiState(
    val nihaiUrunler: List<Item> = emptyList(),
    val selectedItem: Item? = null,
    val quantity: String = "",
    val isSubmitting: Boolean = false,
    val createdOrderId: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProductionOrderCreateViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val productionRepository: ProductionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductionOrderCreateUiState())
    val uiState: StateFlow<ProductionOrderCreateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val items = itemRepository.getItemsByType(ItemType.NIHAI_URUN)
            _uiState.value = _uiState.value.copy(nihaiUrunler = items)
        }
    }

    fun onItemSelected(item: Item) { _uiState.value = _uiState.value.copy(selectedItem = item) }
    fun onQuantityChanged(value: String) { _uiState.value = _uiState.value.copy(quantity = value) }

    fun createDraftOrder() {
        viewModelScope.launch {
            val state = _uiState.value
            val item = state.selectedItem
            val qty = state.quantity.toDoubleOrNull()
            if (item == null || qty == null || qty <= 0) {
                _uiState.value = state.copy(errorMessage = "Ürün ve geçerli bir adet seçilmelidir")
                return@launch
            }
            _uiState.value = state.copy(isSubmitting = true, errorMessage = null)
            try {
                val order = productionRepository.createDraftOrder(item.itemId, qty)
                _uiState.value = _uiState.value.copy(isSubmitting = false, createdOrderId = order.orderId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = e.message)
            }
        }
    }
}

package com.company.crownstock.ui.screens.production

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ProductionOrder
import com.company.crownstock.data.repository.CalculationRepository
import com.company.crownstock.data.repository.ItemRepository
import com.company.crownstock.data.repository.ProductionRepository
import com.company.crownstock.domain.model.ShortageDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #10 (Bölüm 13-14) — ProductionOrderConfirmScreen:
 * "Üretim emrinin onaylanması; onaylanırsa çok seviyeli stok düşümü tetiklenir"
 * Sekans (Bölüm 29.2): üretimiOnayla(orderId) -> StockRepository.stokDüş [Transaction]
 */
data class ProductionOrderConfirmUiState(
    val isLoading: Boolean = true,
    val order: ProductionOrder? = null,
    val targetItem: Item? = null,
    val shortages: List<ShortageDetail> = emptyList(),
    val isConfirming: Boolean = false,
    val confirmed: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProductionOrderConfirmViewModel @Inject constructor(
    private val productionRepository: ProductionRepository,
    private val calculationRepository: CalculationRepository,
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(ProductionOrderConfirmUiState())
    val uiState: StateFlow<ProductionOrderConfirmUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val order = productionRepository.getOrderById(orderId)
                    ?: throw NoSuchElementException("Üretim emri bulunamadı: $orderId")
                val targetItem = itemRepository.getItemById(order.targetItemId)
                // Bölüm 23 — onaydan önce güncel eksik durumu tekrar gösterilir.
                val shortages = calculationRepository.calculateMultiLevelShortage(order.targetItemId, order.requestedQuantity)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    order = order,
                    targetItem = targetItem,
                    shortages = shortages
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun confirm(onConfirmed: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConfirming = true, errorMessage = null)
            try {
                productionRepository.confirmOrder(orderId)
                _uiState.value = _uiState.value.copy(isConfirming = false, confirmed = true)
                onConfirmed()
            } catch (e: Exception) {
                // Bölüm 18 Adım 4c: yetersiz stok durumunda transaction iptal edilir, hata kullanıcıya döner.
                _uiState.value = _uiState.value.copy(isConfirming = false, errorMessage = e.message)
            }
        }
    }
}

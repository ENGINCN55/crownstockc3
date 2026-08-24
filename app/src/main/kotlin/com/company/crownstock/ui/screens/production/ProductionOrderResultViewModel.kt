package com.company.crownstock.ui.screens.production

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.ProductionOrder
import com.company.crownstock.data.model.StockMovement
import com.company.crownstock.data.repository.ProductionRepository
import com.company.crownstock.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #11 (Bölüm 13-14) — ProductionOrderResultScreen:
 * "Üretim sonucu özeti (düşülen stoklar, oluşan yeni ürün stoğu)"
 */
data class ProductionOrderResultUiState(
    val isLoading: Boolean = true,
    val order: ProductionOrder? = null,
    val consumedMovements: List<StockMovement> = emptyList(),
    val outputMovement: StockMovement? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProductionOrderResultViewModel @Inject constructor(
    private val productionRepository: ProductionRepository,
    private val stockRepository: StockRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(ProductionOrderResultUiState())
    val uiState: StateFlow<ProductionOrderResultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val order = productionRepository.getOrderById(orderId)
                val movements = stockRepository.getMovementsForProductionOrder(orderId)
                val consumed = movements.filter { it.movementType == com.company.crownstock.data.model.MovementType.PRODUCTION_CONSUME }
                val output = movements.firstOrNull { it.movementType == com.company.crownstock.data.model.MovementType.PRODUCTION_OUTPUT }
                _uiState.value = ProductionOrderResultUiState(
                    isLoading = false,
                    order = order,
                    consumedMovements = consumed,
                    outputMovement = output
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}

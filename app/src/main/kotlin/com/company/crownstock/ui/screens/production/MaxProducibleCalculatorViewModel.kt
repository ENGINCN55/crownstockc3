package com.company.crownstock.ui.screens.production

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.data.repository.CalculationRepository
import com.company.crownstock.data.repository.ItemRepository
import com.company.crownstock.data.repository.ProductionRepository
import com.company.crownstock.domain.model.BottleneckResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #9 (Bölüm 13-14) — MaxProducibleCalculatorScreen:
 * "Seçilen ürün için 'şu an en fazla kaç adet üretebilirim' sorgusu ve darboğaz
 * bileşeni gösterimi" — Bölüm 20 + Bölüm 21 aynen kullanılır.
 *
 * Bölüm 31 (Uygulama Ekran Akış Diyagramı): ProductionOrderCreateScreen →
 * MaxProducibleCalculatorScreen → ProductionOrderConfirmScreen zinciri. Bu ekran,
 * hesaplanan maksimum adet için doğrudan bir üretim emri taslağı oluşturabilir —
 * ProductionRepository.createDraftOrder ZATEN VAR OLAN metodu yeniden kullanılır,
 * yeni bir iş kuralı eklenmez (Bölüm 29.2 sekansıyla birebir aynı orkestrasyon).
 */
data class MaxProducibleUiState(
    val nihaiUrunler: List<Item> = emptyList(),
    val selectedItem: Item? = null,
    val isLoading: Boolean = false,
    val maxProducibleQuantity: Double? = null,
    val bottleneckRanking: List<BottleneckResult> = emptyList(),
    val isCreatingOrder: Boolean = false,
    val createdOrderId: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class MaxProducibleCalculatorViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val calculationRepository: CalculationRepository,
    private val productionRepository: ProductionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val preselectedItemId: String? = savedStateHandle["itemId"]

    private val _uiState = MutableStateFlow(MaxProducibleUiState())
    val uiState: StateFlow<MaxProducibleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val items = itemRepository.getItemsByType(ItemType.NIHAI_URUN)
            val preselected = preselectedItemId?.let { id -> items.firstOrNull { it.itemId == id } }
            _uiState.value = _uiState.value.copy(nihaiUrunler = items, selectedItem = preselected)
            preselected?.let { calculate(it.itemId) }
        }
    }

    fun onItemSelected(item: Item) {
        _uiState.value = _uiState.value.copy(selectedItem = item)
        calculate(item.itemId)
    }

    private fun calculate(itemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val maxProducible = calculationRepository.calculateMaxProducible(itemId)
                val bottlenecks = calculationRepository.calculateBottleneckRanking(itemId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    maxProducibleQuantity = maxProducible.maxProducibleQuantity,
                    bottleneckRanking = bottlenecks
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    /**
     * Bölüm 31: MaxProducibleCalculatorScreen → ProductionOrderConfirmScreen.
     * Hesaplanan maksimum adet için DRAFT üretim emri oluşturur (IK-7: stok
     * henüz düşülmez) — ProductionRepository.createDraftOrder'ın (Bölüm 29.2)
     * birebir aynısı, burada yalnızca çağrılıyor.
     */
    fun createOrderForMaxQuantity() {
        val state = _uiState.value
        val item = state.selectedItem
        val quantity = state.maxProducibleQuantity
        if (item == null || quantity == null || quantity <= 0) {
            _uiState.value = state.copy(errorMessage = "Üretim emri oluşturmak için önce geçerli bir maksimum adet hesaplanmalıdır")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingOrder = true, errorMessage = null)
            try {
                val order = productionRepository.createDraftOrder(item.itemId, quantity)
                _uiState.value = _uiState.value.copy(isCreatingOrder = false, createdOrderId = order.orderId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCreatingOrder = false, errorMessage = e.message)
            }
        }
    }
}

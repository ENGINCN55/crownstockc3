package com.company.crownstock.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.data.model.ProductionOrder
import com.company.crownstock.data.repository.ItemRepository
import com.company.crownstock.data.repository.ProductionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #1 (Bölüm 13-14) — DashboardScreen:
 * "Genel özet: toplam ürün sayısı, düşük stok uyarı sayısı, son üretim emirleri,
 * hızlı erişim butonları"
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalItemCount: Int = 0,
    val lowStockItems: List<Item> = emptyList(),
    val recentOrders: List<ProductionOrder> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val productionRepository: ProductionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val totalCount = ItemType.entries.sumOf { itemRepository.getItemsByType(it).size }
                val lowStock = itemRepository.getLowStockItems()
                val recentOrders = productionRepository.getRecentOrders(limit = 5)
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    totalItemCount = totalCount,
                    lowStockItems = lowStock,
                    recentOrders = recentOrders
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}

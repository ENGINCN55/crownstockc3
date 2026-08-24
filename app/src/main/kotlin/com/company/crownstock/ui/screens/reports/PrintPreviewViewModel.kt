package com.company.crownstock.ui.screens.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.model.MovementType
import com.company.crownstock.data.repository.ItemRepository
import com.company.crownstock.data.repository.PrintRepository
import com.company.crownstock.data.repository.ReportData
import com.company.crownstock.data.repository.ReportType
import com.company.crownstock.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #14 (Bölüm 13-14) — PrintPreviewScreen:
 * "Stok listesi / eksikler listesi / üretim kapasite analizi / üretim özeti
 * yazdırılabilir önizlemesi; Android Print Framework ile PDF veya yazıcı çıktısı"
 *
 * Not: Android Print Framework (PrintManager/PrintDocumentAdapter) tetiklemesi
 * platform-spesifik bir Activity/Context işlemidir; bu ViewModel yalnızca rapor
 * verisini (ReportData) hazırlar — asıl yazdırma tetiklemesi Composable/Activity
 * katmanında yapılır (Bölüm 24: "MVP'de yazdırma geçmişi tutulmaz").
 */
data class PrintPreviewUiState(
    val isLoading: Boolean = true,
    val report: ReportData? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class PrintPreviewViewModel @Inject constructor(
    private val printRepository: PrintRepository,
    private val itemRepository: ItemRepository,
    private val stockRepository: StockRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reportType: ReportType = ReportType.valueOf(checkNotNull(savedStateHandle["reportType"]))
    private val itemId: String? = savedStateHandle["itemId"]
    private val quantity: Double? = (savedStateHandle.get<String>("quantity"))?.toDoubleOrNull()
    private val orderId: String? = savedStateHandle["orderId"]

    private val _uiState = MutableStateFlow(PrintPreviewUiState())
    val uiState: StateFlow<PrintPreviewUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val report = when (reportType) {
                    ReportType.STOCK_REPORT -> printRepository.prepareStockReport()
                    ReportType.SHORTAGE_LIST -> {
                        val id = checkNotNull(itemId); val qty = quantity ?: 0.0
                        printRepository.prepareShortageListReport(id, qty)
                    }
                    ReportType.CAPACITY_ANALYSIS -> {
                        val id = checkNotNull(itemId)
                        printRepository.prepareCapacityAnalysisReport(id)
                    }
                    ReportType.PRODUCTION_SUMMARY -> {
                        val oId = checkNotNull(orderId)
                        val movements = stockRepository.getMovementsForProductionOrder(oId)
                        val output = movements.firstOrNull { it.movementType == MovementType.PRODUCTION_OUTPUT }
                        val consumed = movements.filter { it.movementType == MovementType.PRODUCTION_CONSUME }
                        val targetItem = output?.let { itemRepository.getItemById(it.itemId) }
                        printRepository.prepareProductionSummaryReport(
                            targetItemId = output?.itemId ?: "",
                            targetItemName = targetItem?.name ?: (output?.itemId ?: ""),
                            confirmedQuantity = output?.quantity ?: 0.0,
                            consumedRawMaterials = consumed.associate { it.itemId to it.quantity }
                        )
                    }
                }
                _uiState.value = _uiState.value.copy(isLoading = false, report = report)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}

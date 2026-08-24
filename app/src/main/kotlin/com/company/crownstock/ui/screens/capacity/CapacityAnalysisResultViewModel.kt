package com.company.crownstock.ui.screens.capacity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.domain.model.CapacityAnalysisResult
import com.company.crownstock.domain.usecase.AnalyzeProductionCapacityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #18 (Bölüm 13-14 / 35.6) — CapacityAnalysisResultScreen:
 * "Analiz sonucu: karşılanabilirlik durumu, maksimum üretilebilir adet,
 * darboğaz sıralaması, eksik yarı mamül listesi; her eksik yarı mamül tıklanabilir."
 */
data class CapacityAnalysisResultUiState(
    val isLoading: Boolean = true,
    val result: CapacityAnalysisResult? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CapacityAnalysisResultViewModel @Inject constructor(
    private val analyzeProductionCapacityUseCase: AnalyzeProductionCapacityUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val targetItemId: String = checkNotNull(savedStateHandle["itemId"])
    private val requestedQuantity: Double = (savedStateHandle.get<String>("quantity"))?.toDoubleOrNull() ?: 0.0

    private val _uiState = MutableStateFlow(CapacityAnalysisResultUiState())
    val uiState: StateFlow<CapacityAnalysisResultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = analyzeProductionCapacityUseCase.execute(targetItemId, requestedQuantity)
                _uiState.value = _uiState.value.copy(isLoading = false, result = result)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}

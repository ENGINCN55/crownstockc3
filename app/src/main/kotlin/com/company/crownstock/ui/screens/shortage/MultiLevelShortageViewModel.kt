package com.company.crownstock.ui.screens.shortage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.crownstock.data.repository.CalculationRepository
import com.company.crownstock.domain.model.ShortageDetail
import com.company.crownstock.domain.usecase.ShortageTreeNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ekran #13 (Bölüm 13-14) — MultiLevelShortageScreen:
 * "Eksikliğin hangi BOM seviyesinde, hangi ana bileşen üzerinden geldiğini
 * ağaç/liste halinde gösterme" — Bölüm 23 Adım 4: "UI'da her iki görünüm de
 * desteklenmelidir".
 */
data class MultiLevelShortageUiState(
    val isLoading: Boolean = true,
    val showTreeView: Boolean = true,
    val flatList: List<ShortageDetail> = emptyList(),
    val tree: List<ShortageTreeNode> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class MultiLevelShortageViewModel @Inject constructor(
    private val calculationRepository: CalculationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val targetItemId: String = checkNotNull(savedStateHandle["itemId"])
    private val requestedQuantity: Double = (savedStateHandle.get<String>("quantity"))?.toDoubleOrNull() ?: 0.0

    private val _uiState = MutableStateFlow(MultiLevelShortageUiState())
    val uiState: StateFlow<MultiLevelShortageUiState> = _uiState.asStateFlow()

    init { load() }

    fun toggleView() { _uiState.value = _uiState.value.copy(showTreeView = !_uiState.value.showTreeView) }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val flat = calculationRepository.calculateMultiLevelShortage(targetItemId, requestedQuantity)
                val tree = calculationRepository.calculateMultiLevelShortageTree(targetItemId, requestedQuantity)
                _uiState.value = _uiState.value.copy(isLoading = false, flatList = flat, tree = tree)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}

package com.company.crownstock.ui.screens.capacity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Ekran #18 (Bölüm 13-14 / 35.6) — CapacityAnalysisResultScreen.
 * Örnek format (Bölüm 35.6) birebir izlendi.
 */
@Composable
fun CapacityAnalysisResultScreen(
    onMissingSemiFinishedClick: (itemId: String, missingQty: Double) -> Unit,
    onPrintClick: () -> Unit,
    viewModel: CapacityAnalysisResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kapasite Analizi Sonucu") },
                actions = {
                    IconButton(onClick = onPrintClick) {
                        Icon(
                            Icons.Filled.Print,
                            contentDescription = "Yazdır"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading || uiState.result == null) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        val result = uiState.result!!

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            item {
                Text("Sipariş: ${result.requestedQuantity} adet", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (result.isFullyFulfillable) "✅ Tam üretilebilir." else "❌ Tam üretilemez.",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    "Şu an üretilebilir: ${result.maxProducibleQuantity} adet",
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text("Darboğaz Sıralaması", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            }
            items(result.bottleneckRanking) { b ->
                Text("${b.itemName} — en fazla ${b.maxUnitsSupportedByThisItem} adet destekler")
            }
            item {
                Text("Eksik Yarı Mamüller (tıklanabilir)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            }
            items(result.missingSemiFinishedItems) { s ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { onMissingSemiFinishedClick(s.itemId, s.missingQty) }
                ) {
                    Text("${s.itemName ?: s.itemId} (-${s.missingQty})", modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}

package com.company.crownstock.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
 * Ekran #1 (Bölüm 13-14) — DashboardScreen.
 * onNavigate: MainGraph'ın diğer graph'larına (ItemsGraph, StockGraph, ProductionGraph,
 * ShortageGraph, ReportsGraph, HistoryGraph, Settings) geçiş için hızlı erişim butonları.
 */
@Composable
fun DashboardScreen(
    onNavigateToItems: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToProductionCreate: () -> Unit,
    onNavigateToCapacityAnalysis: () -> Unit,
    onNavigateToMaxProducible: () -> Unit,
    onNavigateToShortageOverview: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Crown Stok ve Üretim Takip") }) }
    ) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(label = "Toplam Ürün", value = uiState.totalItemCount.toString(), modifier = Modifier.weight(1f))
                    SummaryCard(label = "Düşük Stok Uyarısı", value = uiState.lowStockItems.size.toString(), modifier = Modifier.weight(1f))
                }
            }

            item { Text("Son Üretim Emirleri", style = androidx.compose.material3.MaterialTheme.typography.titleMedium) }
            items(uiState.recentOrders) { order ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Ürün: ${order.targetItemId}")
                        Text("Adet: ${order.requestedQuantity} — Durum: ${order.status}")
                    }
                }
            }

            item { Text("Hızlı Erişim", style = androidx.compose.material3.MaterialTheme.typography.titleMedium) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onNavigateToItems, modifier = Modifier.fillMaxWidth()) { Text("Ürünler") }
                    Button(onClick = onNavigateToStock, modifier = Modifier.fillMaxWidth()) { Text("Stok Durumu") }
                    Button(onClick = onNavigateToProductionCreate, modifier = Modifier.fillMaxWidth()) { Text("Yeni Üretim Emri") }
                    Button(onClick = onNavigateToCapacityAnalysis, modifier = Modifier.fillMaxWidth()) { Text("Kapasite Analizi") }
                    Button(onClick = onNavigateToMaxProducible, modifier = Modifier.fillMaxWidth()) { Text("Maks. Üretilebilir Adet") }
                    Button(onClick = onNavigateToShortageOverview, modifier = Modifier.fillMaxWidth()) { Text("Eksikler (Tek Seviye)") }
                    Button(onClick = onNavigateToHistory, modifier = Modifier.fillMaxWidth()) { Text("İşlem Geçmişi") }
                    Button(onClick = onNavigateToSettings, modifier = Modifier.fillMaxWidth()) { Text("Ayarlar") }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
            Text(label, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        }
    }
}

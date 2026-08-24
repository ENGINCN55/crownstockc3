package com.company.crownstock.ui.screens.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
 * Ekran #3 (Bölüm 13-14) — ItemDetailScreen.
 */
@Composable
fun ItemDetailScreen(
    onEditClick: (String) -> Unit,
    onBomEditorClick: (String) -> Unit,
    onManualStockEntryClick: (String) -> Unit,
    onHistoryClick: (String) -> Unit,
    onMaxProducibleClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text(uiState.item?.name ?: "Ürün Detayı") }) }) { padding ->
        if (uiState.isLoading || uiState.item == null) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        val item = uiState.item!!

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            item {
                Column {
                    Text(item.name, style = MaterialTheme.typography.headlineSmall)
                    Text("Tip: ${item.itemType}")
                    Text("Birim: ${item.unit}")
                    Text("Mevcut Stok: ${item.currentStock}")
                    Text("Minimum Eşik: ${item.minStockThreshold}")
                    item.description?.let { Text("Açıklama: $it") }

                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Button(onClick = { onEditClick(item.itemId) }) { Text("Düzenle") }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))
                        // BOM'a kısayol yalnızca YARI_MAMUL / NIHAI_URUN için anlamlıdır
                        // (HAM_MADDE'nin alt bileşeni olmaz — Bölüm 16).
                        if (item.itemType != com.company.crownstock.data.model.ItemType.HAM_MADDE) {
                            OutlinedButton(onClick = { onBomEditorClick(item.itemId) }) { Text("BOM'u Düzenle") }
                        }
                    }

                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        OutlinedButton(onClick = { onManualStockEntryClick(item.itemId) }) { Text("Manuel Stok Girişi") }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))
                        OutlinedButton(onClick = { onHistoryClick(item.itemId) }) { Text("Tüm Geçmiş") }
                    }

                    if (item.itemType == com.company.crownstock.data.model.ItemType.NIHAI_URUN) {
                        OutlinedButton(
                            onClick = { onMaxProducibleClick(item.itemId) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) { Text("Maks. Üretilebilir Adet") }
                    }

                    Text("Stok Geçmişi Özeti", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp))
                }
            }
            items(uiState.recentMovements) { movement ->
                Text("${movement.movementType}: ${movement.quantity} (sonuç: ${movement.resultingStock})")
            }
        }
    }
}

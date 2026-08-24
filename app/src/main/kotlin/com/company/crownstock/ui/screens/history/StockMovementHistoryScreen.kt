package com.company.crownstock.ui.screens.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.crownstock.data.model.MovementType

/**
 * Ekran #15 (Bölüm 13-14) — StockMovementHistoryScreen.
 */
@Composable
fun StockMovementHistoryScreen(
    viewModel: StockMovementHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("İşlem Geçmişi") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            var itemMenuExpanded by remember { mutableStateOf(false) }
            OutlinedButton(onClick = { itemMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(uiState.selectedItem?.name ?: "Ürün Seç")
            }
            DropdownMenu(expanded = itemMenuExpanded, onDismissRequest = { itemMenuExpanded = false }) {
                uiState.availableItems.forEach { item ->
                    DropdownMenuItem(text = { Text(item.name) }, onClick = { viewModel.onItemSelected(item); itemMenuExpanded = false })
                }
            }

            Row(modifier = Modifier.padding(top = 8.dp)) {
                FilterChip(
                    selected = uiState.movementTypeFilter == null,
                    onClick = { viewModel.onMovementTypeFilterChanged(null) },
                    label = { Text("Tümü") }
                )
                MovementType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.movementTypeFilter == type,
                        onClick = { viewModel.onMovementTypeFilterChanged(type) },
                        label = { Text(type.name) },
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                    items(uiState.movements) { m ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("${m.movementType} — ${m.quantity}")
                                Text("Sonuç stok: ${m.resultingStock}")
                                m.reason?.let { Text("Neden: $it") }
                                Text("${m.timestamp}")
                            }
                        }
                    }
                }
            }
        }
    }
}

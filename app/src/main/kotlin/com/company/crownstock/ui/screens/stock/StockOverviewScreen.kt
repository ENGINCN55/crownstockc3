package com.company.crownstock.ui.screens.stock

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Ekran #6 (Bölüm 13-14) — StockOverviewScreen.
 */
@Composable
fun StockOverviewScreen(
    onManualEntryClick: () -> Unit,
    onPrintClick: () -> Unit,
    viewModel: StockOverviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stok Durumu") },
                actions = {
                    IconButton(onClick = onPrintClick) {
                        Icon(Icons.Filled.Print, contentDescription = "Yazdır")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onManualEntryClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Manuel Stok Girişi")
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(padding)) {
            FilterChip(
                selected = uiState.lowStockOnly,
                onClick = viewModel::toggleLowStockOnly,
                label = { Text("Yalnızca Düşük Stok") },
                modifier = Modifier.padding(16.dp)
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.items) { item ->
                        val isLow = item.currentStock < item.minStockThreshold
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                            Row(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                Text(item.name, modifier = Modifier.weight(1f))
                                Text(
                                    "${item.currentStock} ${item.unit}",
                                    color = if (isLow) Color.Red else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

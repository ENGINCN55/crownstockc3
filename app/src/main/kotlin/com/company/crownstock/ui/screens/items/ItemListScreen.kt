package com.company.crownstock.ui.screens.items

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType

/**
 * Ekran #2 (Bölüm 13-14) — ItemListScreen.
 */
@Composable
fun ItemListScreen(
    onItemClick: (String) -> Unit,
    onAddItemClick: () -> Unit,
    viewModel: ItemListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ürünler") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItemClick) {
                Icon(Icons.Filled.Add, contentDescription = "Yeni Ürün")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("Ara") },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                FilterChip(
                    selected = uiState.selectedType == null,
                    onClick = { viewModel.onTypeSelected(null) },
                    label = { Text("Tümü") }
                )
                ItemType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.selectedType == type,
                        onClick = { viewModel.onTypeSelected(type) },
                        label = { Text(type.name) },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.items) { item -> ItemRow(item = item, onClick = { onItemClick(item.itemId) }) }
                }
            }
        }
    }
}

@Composable
private fun ItemRow(item: Item, onClick: () -> Unit) {
    // Stok durumu göstergesi: currentStock < minStockThreshold ise düşük stok (kırmızı).
    val isLowStock = item.currentStock < item.minStockThreshold
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text(item.itemType.name, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "${item.currentStock} ${item.unit}",
                color = if (isLowStock) Color.Red else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

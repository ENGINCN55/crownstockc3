package com.company.crownstock.ui.screens.production

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Ekran #9 (Bölüm 13-14) — MaxProducibleCalculatorScreen.
 */
@Composable
fun MaxProducibleCalculatorScreen(
    onOrderCreated: (orderId: String) -> Unit,
    viewModel: MaxProducibleCalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.createdOrderId) {
        uiState.createdOrderId?.let { onOrderCreated(it) }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Maksimum Üretilebilir Adet") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            var expanded by remember { mutableStateOf(false) }
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(uiState.selectedItem?.name ?: "Nihai Ürün Seç")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                uiState.nihaiUrunler.forEach { item ->
                    DropdownMenuItem(text = { Text(item.name) }, onClick = { viewModel.onItemSelected(item); expanded = false })
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }

            uiState.maxProducibleQuantity?.let { max ->
                Text(
                    "Şu an üretilebilir: $max adet",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
                if (max > 0) {
                    androidx.compose.material3.Button(
                        onClick = viewModel::createOrderForMaxQuantity,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        enabled = !uiState.isCreatingOrder
                    ) { Text("Bu Adet İçin Üretim Emri Oluştur") }
                }
            }

            if (uiState.bottleneckRanking.isNotEmpty()) {
                Text("Darboğaz Sıralaması", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                LazyColumn {
                    items(uiState.bottleneckRanking) { b ->
                        Text("${b.itemName} — en fazla ${b.maxUnitsSupportedByThisItem} adet destekler")
                    }
                }
            }

            uiState.errorMessage?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp)) }
        }
    }
}

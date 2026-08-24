package com.company.crownstock.ui.screens.shortage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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

/**
 * Ekran #12 (Bölüm 13-14) — ShortageOverviewScreen.
 * Bölüm 31: DashboardScreen'den doğrudan erişilir — ürün/adet ekranın içinde seçilir
 * (önceden bir itemId/quantity verilmişse otomatik hesaplanır, bkz. ViewModel).
 */
@Composable
fun ShortageOverviewScreen(
    onComponentClick: (itemId: String, missingQty: Double) -> Unit,
    viewModel: ShortageOverviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Eksikler (Tek Seviye)") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            var expanded by remember { mutableStateOf(false) }
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(uiState.selectedItem?.name ?: "Nihai Ürün Seç")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                uiState.nihaiUrunler.forEach { item ->
                    DropdownMenuItem(text = { Text(item.name) }, onClick = { viewModel.onItemSelected(item); expanded = false })
                }
            }

            OutlinedTextField(
                value = uiState.requestedQuantity,
                onValueChange = viewModel::onQuantityChanged,
                label = { Text("Talep Edilen Miktar") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            uiState.errorMessage?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp)) }

            Button(onClick = viewModel::calculateClicked, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Eksikleri Hesapla")
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            } else if (uiState.hasCalculated) {
                if (uiState.shortages.isEmpty()) {
                    Text("✅ Doğrudan bileşenlerde eksik yok.", modifier = Modifier.padding(top = 16.dp))
                } else {
                    LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                        items(uiState.shortages) { s ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                onClick = { onComponentClick(s.itemId, s.missingQty) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(s.itemName ?: s.itemId)
                                    Text("Gereken: ${s.requiredQty} — Mevcut: ${s.availableQty} — Eksik: ${s.missingQty}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

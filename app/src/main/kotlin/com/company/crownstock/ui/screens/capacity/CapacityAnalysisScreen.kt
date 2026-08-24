package com.company.crownstock.ui.screens.capacity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
 * Ekran #17 (Bölüm 13-14 / 35.6) — CapacityAnalysisScreen.
 * "Herhangi bir onay/üretim butonu bulunmaz (iş kuralı 3-4 gereği)."
 */
@Composable
fun CapacityAnalysisScreen(
    onCheckCapacity: (itemId: String, quantity: Double) -> Unit,
    onViewDirectShortages: (itemId: String, quantity: Double) -> Unit,
    viewModel: CapacityAnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Kapasite Analizi") }) }) { padding ->
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

            OutlinedTextField(
                value = uiState.quantity,
                onValueChange = viewModel::onQuantityChanged,
                label = { Text("Sipariş Miktarı") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            uiState.errorMessage?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp)) }

            Button(
                onClick = { viewModel.validateAndProceed()?.let { (itemId, qty) -> onCheckCapacity(itemId, qty) } },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) { Text("Kapasite Kontrol Et") }

            OutlinedButton(
                onClick = { viewModel.validateAndProceed()?.let { (itemId, qty) -> onViewDirectShortages(itemId, qty) } },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) { Text("Doğrudan Bileşen Eksiklerini Gör (Tek Seviye)") }
        }
    }
}

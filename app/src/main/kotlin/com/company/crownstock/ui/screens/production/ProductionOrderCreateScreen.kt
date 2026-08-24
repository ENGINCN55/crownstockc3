package com.company.crownstock.ui.screens.production

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
 * Ekran #8 (Bölüm 13-14) — ProductionOrderCreateScreen.
 * Taslak emir oluşturulunca ProductionOrderConfirmScreen'e yönlendirilir
 * (orada eksik/darboğaz detayları ve onay butonu gösterilir — Bölüm 12).
 */
@Composable
fun ProductionOrderCreateScreen(
    onDraftCreated: (orderId: String) -> Unit,
    onCheckMaxProducible: (itemId: String) -> Unit,
    onCheckCapacityAnalysis: () -> Unit,
    viewModel: ProductionOrderCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.createdOrderId) {
        uiState.createdOrderId?.let { onDraftCreated(it) }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Yeni Üretim Emri") }) }) { padding ->
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
                label = { Text("Üretilecek Adet") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            uiState.errorMessage?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp)) }

            uiState.selectedItem?.let { item ->
                OutlinedButton(
                    onClick = { onCheckMaxProducible(item.itemId) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) { Text("Şu An Kaç Adet Üretebilirim?") }
            }

            OutlinedButton(
                onClick = onCheckCapacityAnalysis,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) { Text("Kapasite Analizi (Bölüm 35)") }

            Button(
                onClick = viewModel::createDraftOrder,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = !uiState.isSubmitting
            ) { Text("Devam Et (Eksik/Darboğaz Kontrolü)") }
        }
    }
}

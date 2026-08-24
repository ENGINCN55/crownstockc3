package com.company.crownstock.ui.screens.production

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
 * Ekran #10 (Bölüm 13-14) — ProductionOrderConfirmScreen.
 */
@Composable
fun ProductionOrderConfirmScreen(
    onConfirmed: () -> Unit,
    viewModel: ProductionOrderConfirmViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Üretim Emrini Onayla") }) }) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Ürün: ${uiState.targetItem?.name ?: uiState.order?.targetItemId}", style = MaterialTheme.typography.titleMedium)
            Text("Adet: ${uiState.order?.requestedQuantity}")

            if (uiState.shortages.isNotEmpty()) {
                Text(
                    "⚠️ Eksik ham maddeler tespit edildi:",
                    color = androidx.compose.ui.graphics.Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(uiState.shortages) { s ->
                        Text("${s.itemName ?: s.itemId}: eksik ${s.missingQty}")
                    }
                }
            } else {
                Text("✅ Tüm bileşenler yeterli.", modifier = Modifier.padding(top = 16.dp))
            }

            uiState.errorMessage?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp)) }

            Button(
                onClick = { viewModel.confirm(onConfirmed) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = !uiState.isConfirming
            ) { Text("Onayla ve Üretimi Tamamla") }
        }
    }
}

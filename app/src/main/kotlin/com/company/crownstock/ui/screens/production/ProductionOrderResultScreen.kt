package com.company.crownstock.ui.screens.production

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
 * Ekran #11 (Bölüm 13-14) — ProductionOrderResultScreen.
 */
@Composable
fun ProductionOrderResultScreen(
    onPrintClick: () -> Unit,
    viewModel: ProductionOrderResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Üretim Sonucu") },
                actions = {
                    IconButton(onClick = onPrintClick) {
                        Icon(
                            Icons.Filled.Print,
                            contentDescription = "Yazdır"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("✅ Üretim tamamlandı: ${uiState.order?.status}", style = MaterialTheme.typography.titleMedium)

            uiState.outputMovement?.let {
                Text(
                    "Oluşan yeni ürün stoğu: ${it.resultingStock} (eklenen: ${it.quantity})",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Text("Düşülen Stoklar", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            LazyColumn {
                items(uiState.consumedMovements) { m ->
                    Text("${m.itemId}: -${m.quantity} (kalan: ${m.resultingStock})")
                }
            }
        }
    }
}

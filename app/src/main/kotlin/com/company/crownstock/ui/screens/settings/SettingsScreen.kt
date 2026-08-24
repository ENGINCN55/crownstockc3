package com.company.crownstock.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
 * Ekran #16 (Bölüm 13-14) — SettingsScreen.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Ayarlar") }) }) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = uiState.globalLowStockThreshold,
                onValueChange = viewModel::onThresholdChanged,
                label = { Text("Genel Düşük Stok Eşiği") },
                modifier = Modifier.fillMaxWidth()
            )
            uiState.errorMessage?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp)) }
            if (uiState.isSaved) Text("Kaydedildi.", modifier = Modifier.padding(top = 8.dp))
            Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Kaydet") }
        }
    }
}

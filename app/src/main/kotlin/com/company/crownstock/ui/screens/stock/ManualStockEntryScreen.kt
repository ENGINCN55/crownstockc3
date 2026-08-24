package com.company.crownstock.ui.screens.stock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import com.company.crownstock.data.model.MovementType

/**
 * Ekran #7 (Bölüm 13-14) — ManualStockEntryScreen.
 */
@Composable
fun ManualStockEntryScreen(
    onSaved: () -> Unit,
    viewModel: ManualStockEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Manuel Stok Girişi/Çıkışı") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            var expanded by remember { mutableStateOf(false) }
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(uiState.selectedItem?.name ?: "Ürün Seç")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                uiState.availableItems.forEach { item ->
                    DropdownMenuItem(text = { Text(item.name) }, onClick = { viewModel.onItemSelected(item); expanded = false })
                }
            }

            Row(modifier = Modifier.padding(top = 8.dp)) {
                Row {
                    RadioButton(selected = uiState.movementType == MovementType.MANUAL_IN, onClick = { viewModel.onMovementTypeChanged(MovementType.MANUAL_IN) })
                    Text("Giriş", modifier = Modifier.padding(top = 12.dp, end = 16.dp))
                }
                Row {
                    RadioButton(selected = uiState.movementType == MovementType.MANUAL_OUT, onClick = { viewModel.onMovementTypeChanged(MovementType.MANUAL_OUT) })
                    Text("Çıkış", modifier = Modifier.padding(top = 12.dp))
                }
            }

            OutlinedTextField(
                value = uiState.quantity,
                onValueChange = viewModel::onQuantityChanged,
                label = { Text("Miktar") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            OutlinedTextField(
                value = uiState.reason,
                onValueChange = viewModel::onReasonChanged,
                label = { Text("Neden") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            if (uiState.lowStockWarning) {
                Text("Uyarı: Yeni stok, minimum eşiğin altında!", color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
            uiState.errorMessage?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp)) }

            Button(onClick = viewModel::submit, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text("Kaydet")
            }
        }
    }
}

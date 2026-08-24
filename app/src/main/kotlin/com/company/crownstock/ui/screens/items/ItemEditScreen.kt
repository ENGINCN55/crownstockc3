package com.company.crownstock.ui.screens.items

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
import com.company.crownstock.data.model.ItemType

/**
 * Ekran #4 (Bölüm 13-14) — ItemEditScreen.
 */
@Composable
fun ItemEditScreen(
    onSaved: () -> Unit,
    viewModel: ItemEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Scaffold(topBar = { TopAppBar(title = { Text(if (uiState.isEditMode) "Ürünü Düzenle" else "Yeni Ürün") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Ürün Adı") },
                modifier = Modifier.fillMaxWidth()
            )

            var typeMenuExpanded by remember { mutableStateOf(false) }
            OutlinedButton(onClick = { typeMenuExpanded = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Tip: ${uiState.itemType}")
            }
            DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                ItemType.entries.forEach { type ->
                    DropdownMenuItem(text = { Text(type.name) }, onClick = {
                        viewModel.onTypeChanged(type)
                        typeMenuExpanded = false
                    })
                }
            }

            OutlinedTextField(
                value = uiState.unit,
                onValueChange = viewModel::onUnitChanged,
                label = { Text("Birim (adet, metre, ...)") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            OutlinedTextField(
                value = uiState.minStockThreshold,
                onValueChange = viewModel::onMinStockThresholdChanged,
                label = { Text("Minimum Stok Eşiği") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Açıklama (opsiyonel)") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            uiState.errorMessage?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp)) }

            Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text("Kaydet")
            }
        }
    }
}

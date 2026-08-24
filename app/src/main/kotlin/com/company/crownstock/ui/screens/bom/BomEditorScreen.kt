package com.company.crownstock.ui.screens.bom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.company.crownstock.data.model.Item

/**
 * Ekran #5 (Bölüm 13-14) — BomEditorScreen.
 */
@Composable
fun BomEditorScreen(
    onBack: () -> Unit,
    viewModel: BomEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("BOM: ${uiState.parentItem?.name ?: ""}") }) }) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            uiState.errorMessage?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red) }

            AddComponentForm(availableItems = uiState.availableChildItems, onAdd = viewModel::addComponent)

            Text("Bileşenler", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.components) { row ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(row.childItem?.name ?: row.component.childItemId)
                                Text("${row.component.quantityPerUnit} ${row.component.unit}")
                            }
                            IconButton(onClick = { viewModel.removeComponent(row.component.bomId) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Sil")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddComponentForm(availableItems: List<Item>, onAdd: (childItemId: String, quantityPerUnit: Double, unit: String) -> Unit) {
    var selected by remember { mutableStateOf<Item?>(null) }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("adet") }
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected?.name ?: "Bileşen Seç")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            availableItems.forEach { item ->
                DropdownMenuItem(text = { Text(item.name) }, onClick = { selected = item; expanded = false })
            }
        }
        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Miktar (1 adet ana ürün için)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Birim") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        Button(
            onClick = {
                val qty = quantity.toDoubleOrNull()
                if (selected != null && qty != null && qty > 0) {
                    onAdd(selected!!.itemId, qty, unit)
                    selected = null
                    quantity = ""
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("Bileşen Ekle") }
    }
}

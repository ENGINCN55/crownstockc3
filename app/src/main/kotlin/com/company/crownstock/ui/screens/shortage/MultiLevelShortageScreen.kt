package com.company.crownstock.ui.screens.shortage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.crownstock.domain.usecase.ShortageTreeNode

/**
 * Ekran #13 (Bölüm 13-14) — MultiLevelShortageScreen.
 * Bölüm 23 Adım 4: "ağaç görünümü ... veya düzleştirilmiş liste ... UI'da her
 * iki görünüm de desteklenmelidir" — ekranda iki görünüm arasında geçiş butonu var.
 */
@Composable
fun MultiLevelShortageScreen(
    onPrintClick: () -> Unit,
    viewModel: MultiLevelShortageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Çok Seviyeli Eksikler") },
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
            Button(onClick = viewModel::toggleView) {
                Text(if (uiState.showTreeView) "Düzleştirilmiş Listeye Geç" else "Ağaç Görünümüne Geç")
            }

            uiState.errorMessage?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red) }

            if (uiState.showTreeView) {
                LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                    items(uiState.tree) { node -> ShortageTreeNodeRow(node, depth = 0) }
                }
            } else {
                LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                    items(uiState.flatList) { s ->
                        Text("[Seviye ${s.level}] ${s.itemName ?: s.itemId}: eksik ${s.missingQty} (gereken ${s.requiredQty}, mevcut ${s.availableQty})")
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortageTreeNodeRow(node: ShortageTreeNode, depth: Int) {
    Column(modifier = Modifier.padding(start = (depth * 16).dp, top = 4.dp)) {
        Text("${node.itemName ?: node.itemId}: eksik ${node.missingQty} (gereken ${node.requiredQty}, mevcut ${node.availableQty})")
        node.children.forEach { child -> ShortageTreeNodeRow(child, depth + 1) }
    }
}

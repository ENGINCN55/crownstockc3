package com.company.crownstock.ui.screens.reports

import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.crownstock.data.repository.ReportData

/**
 * Ekran #14 (Bölüm 13-14) — PrintPreviewScreen.
 * "Android Print Framework (PrintManager / PrintDocumentAdapter) kullanılır.
 * Compose UI'dan render edilen önizleme, PDF veya yazıcı çıktısına aktarılır." (Bölüm 24)
 *
 * Uygulama: rapor verisi basit bir HTML tablosuna dönüştürülür ve gizli bir
 * WebView'ın oluşturduğu PrintDocumentAdapter, PrintManager'a verilir — bu,
 * Android'de yaygın kullanılan standart bir yazdırma tekniğidir.
 */
@Composable
fun PrintPreviewScreen(
    viewModel: PrintPreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Yazdırma Önizleme") }) }
    ) { padding ->
        if (uiState.isLoading || uiState.report == null) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        val report = uiState.report!!

        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(report.title, style = MaterialTheme.typography.headlineSmall)
            Text(report.generatedAt.toString(), style = MaterialTheme.typography.bodySmall)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Text(report.columnHeaders.joinToString(" | "), style = MaterialTheme.typography.titleSmall)
                    Divider()
                }
                items(report.rows) { row -> Text(row.columns.joinToString(" | ")) }
                report.summary?.let { s -> item { Text(s, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleMedium) } }
            }

            Button(
                onClick = { triggerAndroidPrint(context, report) },
                modifier = Modifier.padding(top = 16.dp)
            ) { Text("Yazdır") }
        }
    }
}

private fun buildHtml(report: ReportData): String {
    val headerHtml = report.columnHeaders.joinToString("") { "<th>$it</th>" }
    val rowsHtml = report.rows.joinToString("") { row ->
        "<tr>" + row.columns.joinToString("") { "<td>$it</td>" } + "</tr>"
    }
    val summaryHtml = report.summary?.let { "<p><b>$it</b></p>" } ?: ""
    return """
        <html><body>
        <h2>${report.title}</h2>
        <p>${report.generatedAt}</p>
        <table border="1" cellspacing="0" cellpadding="4">
        <tr>$headerHtml</tr>
        $rowsHtml
        </table>
        $summaryHtml
        </body></html>
    """.trimIndent()
}

private fun triggerAndroidPrint(context: android.content.Context, report: ReportData) {
    val webView = WebView(context)
    webView.loadDataWithBaseURL(null, buildHtml(report), "text/HTML", "UTF-8", null)
    val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as PrintManager
    val jobName = "${report.reportType.name}_${System.currentTimeMillis()}"
    val printAdapter = webView.createPrintDocumentAdapter(jobName)
    printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
}

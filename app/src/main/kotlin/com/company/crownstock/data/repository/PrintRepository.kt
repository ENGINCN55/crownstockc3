package com.company.crownstock.data.repository

import com.company.crownstock.data.model.ItemType
import java.util.Date

/**
 * Bölüm 24 — Yazdırma Altyapısı Planı (MVP):
 * "Yazdırılacak veri, PrintRepository tarafından yapılandırılmış bir rapor
 * modeline dönüştürülür (başlık, tarih, tablo satırları, toplam özet)."
 * "MVP'de yazdırma geçmişi tutulmaz; printJobs koleksiyonu kullanılmaz ve
 * Firestore'a herhangi bir yazdırma kaydı yazılmaz."
 *
 * Bu repository yalnızca rapor verisi HAZIRLAR; Android Print Framework
 * (PrintManager/PrintDocumentAdapter) tetiklemesi UI/PrintPreviewScreen
 * sorumluluğundadır (Bölüm 24).
 */
enum class ReportType {
    STOCK_REPORT,
    SHORTAGE_LIST,
    CAPACITY_ANALYSIS,
    PRODUCTION_SUMMARY
}

data class ReportRow(val columns: List<String>)

data class ReportData(
    val reportType: ReportType,
    val title: String,
    val generatedAt: Date,
    val columnHeaders: List<String>,
    val rows: List<ReportRow>,
    val summary: String? = null
)

class PrintRepository(
    private val itemRepository: ItemRepository,
    private val calculationRepository: CalculationRepository
) {

    /** STOCK_REPORT — Stok listesi. */
    suspend fun prepareStockReport(): ReportData {
        val items = ItemType.entries.flatMap { itemRepository.getItemsByType(it) }
        return ReportData(
            reportType = ReportType.STOCK_REPORT,
            title = "Stok Listesi",
            generatedAt = Date(),
            columnHeaders = listOf("Ürün", "Tip", "Birim", "Mevcut Stok", "Eşik"),
            rows = items.map {
                ReportRow(
                    listOf(it.name, it.itemType.name, it.unit, it.currentStock.toString(), it.minStockThreshold.toString())
                )
            }
        )
    }

    /** SHORTAGE_LIST — Eksikler listesi (çok seviyeli, Bölüm 23). */
    suspend fun prepareShortageListReport(targetItemId: String, requestedQuantity: Double): ReportData {
        val shortages = calculationRepository.calculateMultiLevelShortage(targetItemId, requestedQuantity)
        return ReportData(
            reportType = ReportType.SHORTAGE_LIST,
            title = "Eksikler Listesi",
            generatedAt = Date(),
            columnHeaders = listOf("Ürün", "Seviye", "Gereken", "Mevcut", "Eksik"),
            rows = shortages.map {
                ReportRow(
                    listOf(
                        it.itemName ?: it.itemId,
                        it.level.toString(),
                        it.requiredQty.toString(),
                        it.availableQty.toString(),
                        it.missingQty.toString()
                    )
                )
            }
        )
    }

    /** CAPACITY_ANALYSIS — Üretim kapasite analizi (Bölüm 20-21). */
    suspend fun prepareCapacityAnalysisReport(targetItemId: String): ReportData {
        val maxProducible = calculationRepository.calculateMaxProducible(targetItemId)
        val bottlenecks = calculationRepository.calculateBottleneckRanking(targetItemId)
        return ReportData(
            reportType = ReportType.CAPACITY_ANALYSIS,
            title = "Üretim Kapasite Analizi",
            generatedAt = Date(),
            columnHeaders = listOf("Ürün", "Mevcut Stok", "Birim Başına Gereken", "Desteklenen Maks. Adet"),
            rows = bottlenecks.map {
                ReportRow(
                    listOf(it.itemName, it.availableStock.toString(), it.requiredPerUnit.toString(), it.maxUnitsSupportedByThisItem.toString())
                )
            },
            summary = "Maksimum üretilebilir adet: ${maxProducible.maxProducibleQuantity}"
        )
    }

    /** PRODUCTION_SUMMARY — Üretim özeti. */
    fun prepareProductionSummaryReport(
        targetItemId: String,
        targetItemName: String,
        confirmedQuantity: Double,
        consumedRawMaterials: Map<String, Double>
    ): ReportData {
        return ReportData(
            reportType = ReportType.PRODUCTION_SUMMARY,
            title = "Üretim Özeti",
            generatedAt = Date(),
            columnHeaders = listOf("Ham Madde", "Düşülen Miktar"),
            rows = consumedRawMaterials.map { (itemId, qty) -> ReportRow(listOf(itemId, qty.toString())) },
            summary = "$targetItemName: $confirmedQuantity adet üretildi."
        )
    }
}

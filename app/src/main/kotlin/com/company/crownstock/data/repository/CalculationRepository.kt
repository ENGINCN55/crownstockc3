package com.company.crownstock.data.repository

import com.company.crownstock.data.datasource.BomDataSource
import com.company.crownstock.data.datasource.ItemDataSource
import com.company.crownstock.data.model.Item
import com.company.crownstock.domain.model.BomTreeNode
import com.company.crownstock.domain.model.BottleneckResult
import com.company.crownstock.domain.model.MaxProducibleResult
import com.company.crownstock.domain.model.ShortageDetail
import com.company.crownstock.domain.usecase.BuildBomTreeUseCase
import com.company.crownstock.domain.usecase.CalculateBottleneckUseCase
import com.company.crownstock.domain.usecase.CalculateDirectShortageUseCase
import com.company.crownstock.domain.usecase.CalculateMaxProducibleUseCase
import com.company.crownstock.domain.usecase.CalculateMultiLevelShortageUseCase
import com.company.crownstock.domain.usecase.ShortageTreeNode

/**
 * Bölüm 8 — CalculationRepository: Maksimum üretilebilir adet, darboğaz,
 * çok seviyeli eksik hesaplama algoritmalarını barındırır (Bölüm 20-23).
 *
 * Bölüm 29.2 (Sekans Diyagramı — Üretim Onay Akışı) UYUMLULUĞU: doküman açıkça
 * "CalculationRepository → BomRepository : bomAğacınıGetir(targetItemId)" akışını
 * tanımlıyor. Önceki sürümde CalculationRepository kendi BuildBomTreeUseCase
 * örneğini tutuyordu (BomRepository'dekiyle AYNI mantığı tekrar ediyordu) — bu
 * kod tekrarı giderildi: BOM ağacı artık yalnızca BomRepository üzerinden alınır,
 * tek merkezde toplandı.
 *
 * Bölüm 35 notu: Üretim Kapasite Analizi modülü bu repository'nin metodlarını
 * olduğu gibi çağırır; burada yeni bir hesaplama metodu eklenmedi.
 */
class CalculationRepository(
    private val bomRepository: BomRepository,
    private val itemDataSource: ItemDataSource,
    bomDataSource: BomDataSource
) {
    private val calculateMaxProducibleUseCase = CalculateMaxProducibleUseCase()
    private val calculateBottleneckUseCase = CalculateBottleneckUseCase()
    private val calculateDirectShortageUseCase = CalculateDirectShortageUseCase(bomDataSource, itemDataSource)
    private val calculateMultiLevelShortageUseCase = CalculateMultiLevelShortageUseCase()

    /** Bölüm 15.1 / 29.2 — ağaç, tek merkez olan BomRepository üzerinden alınır. */
    suspend fun buildBomTree(targetItemId: String): BomTreeNode = bomRepository.getBomTree(targetItemId)

    /** Bölüm 15.1 / S1: ağaçtaki TÜM düğümlerin (yarı mamül + ham madde) güncel
     * Item kayıtlarını getirir. StockRepository, S1 stok-öncelikli tüketim
     * planı için buna ihtiyaç duyar (yalnızca ham maddeler değil). */
    suspend fun fetchItemsById(root: BomTreeNode): Map<String, Item> {
        val ids = BuildBomTreeUseCase.collectItemIds(root)
        return ids.mapNotNull { id -> itemDataSource.getItemById(id)?.let { id to it } }.toMap()
    }

    /** Yalnızca ağaç yürüyüşü (Firestore okuması yok) — S1 transaction'ının hangi
     * doküman id'lerini transaction.get() ile okuması gerektiğini belirlemek için. */
    fun collectTreeItemIds(root: BomTreeNode): Set<String> = BuildBomTreeUseCase.collectItemIds(root)

    /** Bölüm 20 */
    suspend fun calculateMaxProducible(targetItemId: String): MaxProducibleResult {
        val root = buildBomTree(targetItemId)
        val itemsById = fetchItemsById(root)
        return calculateMaxProducibleUseCase.execute(root, itemsById)
    }

    /** Bölüm 21 */
    suspend fun calculateBottleneckRanking(targetItemId: String): List<BottleneckResult> {
        val root = buildBomTree(targetItemId)
        val itemsById = fetchItemsById(root)
        return calculateBottleneckUseCase.execute(root, itemsById)
    }

    /** Bölüm 22 (tek seviye, recursive değil) */
    suspend fun calculateDirectShortage(targetItemId: String, requestedQuantity: Double): List<ShortageDetail> {
        return calculateDirectShortageUseCase.execute(targetItemId, requestedQuantity)
    }

    /** Bölüm 23 (çok seviyeli, düzleştirilmiş liste görünümü) */
    suspend fun calculateMultiLevelShortage(targetItemId: String, requestedQuantity: Double): List<ShortageDetail> {
        val root = buildBomTree(targetItemId)
        val itemsById = fetchItemsById(root)
        return calculateMultiLevelShortageUseCase.executeAsFlatList(root, requestedQuantity, itemsById)
    }

    /** Bölüm 23 (çok seviyeli, ağaç görünümü) */
    suspend fun calculateMultiLevelShortageTree(targetItemId: String, requestedQuantity: Double): List<ShortageTreeNode> {
        val root = buildBomTree(targetItemId)
        val itemsById = fetchItemsById(root)
        return calculateMultiLevelShortageUseCase.executeAsTree(root, requestedQuantity, itemsById)
    }
}

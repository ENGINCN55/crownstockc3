package com.company.crownstock.data.repository

import com.company.crownstock.data.datasource.ProductionOrderDataSource
import com.company.crownstock.data.model.ProductionOrder
import com.company.crownstock.data.model.ProductionOrderStatus
import com.company.crownstock.domain.model.ShortageDetail

/**
 * Bölüm 8 — ProductionRepository: Üretim emri oluşturma, onaylama, iptal etme;
 * üretim onayında StockRepository ve BomRepository/CalculationRepository'yi
 * orkestra eder (Bölüm 29.2 — Sekans Diyagramı: Üretim Onay Akışı ile birebir).
 */
class ProductionRepository(
    private val productionOrderDataSource: ProductionOrderDataSource,
    private val calculationRepository: CalculationRepository,
    private val stockRepository: StockRepository
) {

    /**
     * Sekans: ProductionOrderCreateScreen -> hesaplaEksik(targetItemId, adet)
     * -> CalculationRepository.çokSeviyeliEksikHesapla(...) (Bölüm 23)
     * IK-7: DRAFT aşamasında stok düşülmez, yalnızca kontrol edilir.
     */
    suspend fun createDraftOrder(targetItemId: String, requestedQuantity: Double): ProductionOrder {
        val shortages: List<ShortageDetail> =
            calculationRepository.calculateMultiLevelShortage(targetItemId, requestedQuantity)
        val bottlenecks = calculationRepository.calculateBottleneckRanking(targetItemId)

        val order = ProductionOrder(
            targetItemId = targetItemId,
            requestedQuantity = requestedQuantity,
            status = ProductionOrderStatus.DRAFT,
            shortageDetected = shortages.isNotEmpty(),
            bottleneckItemIds = bottlenecks
                .filter { it.maxUnitsSupportedByThisItem == (bottlenecks.minOfOrNull { b -> b.maxUnitsSupportedByThisItem } ?: 0.0) }
                .map { it.itemId }
        )
        val orderId = productionOrderDataSource.createOrder(order)
        return order.copy(orderId = orderId)
    }

    /**
     * Sekans: ProductionOrderConfirmScreen -> üretimiOnayla(orderId)
     * -> StockRepository.stokDüş(BomTree, adet) [Transaction] (Bölüm 18)
     * -> productionOrders.status = COMPLETED
     *
     * Not: IK-7 gereği CONFIRMED durumuna geçiş burada ele alınır; Bölüm 18'in
     * "ön koşulu" (CONFIRMED olmalı) bu akışla sağlanır.
     *
     * TUTARLILIK DÜZELTMESİ: confirmProduction (Firestore transaction) stok
     * yetersizliği veya başka bir nedenle başarısız olursa, emir CONFIRMED
     * durumunda askıda kalmamalıdır (Bölüm 4.3.4'te böyle bir "askıda/başarısız"
     * durum tanımlanmamıştır). Bu yüzden hata durumunda emir DRAFT'a geri alınır
     * ve orijinal hata yeniden fırlatılır — kullanıcı tekrar deneyebilir.
     */
    suspend fun confirmOrder(orderId: String) {
        val order = productionOrderDataSource.getOrderById(orderId)
            ?: throw NoSuchElementException("Üretim emri bulunamadı: $orderId")

        productionOrderDataSource.updateOrderStatus(orderId, ProductionOrderStatus.CONFIRMED)

        try {
            stockRepository.confirmProduction(
                targetItemId = order.targetItemId,
                confirmedQuantity = order.requestedQuantity,
                relatedProductionOrderId = orderId
            )
        } catch (e: Exception) {
            // Stok düşümü (Firestore transaction) başarısız oldu: DRAFT'a geri al.
            productionOrderDataSource.updateOrderStatus(orderId, ProductionOrderStatus.DRAFT)
            throw e
        }

        // Bölüm 18 Adım 6: productionOrders kaydı COMPLETED olarak güncellenir.
        productionOrderDataSource.updateOrderStatus(orderId, ProductionOrderStatus.COMPLETED)
    }

    suspend fun cancelOrder(orderId: String) {
        productionOrderDataSource.updateOrderStatus(orderId, ProductionOrderStatus.CANCELLED)
    }

    suspend fun getOrdersByStatus(status: ProductionOrderStatus): List<ProductionOrder> =
        productionOrderDataSource.getOrdersByStatus(status)

    suspend fun getOrderById(orderId: String): ProductionOrder? =
        productionOrderDataSource.getOrderById(orderId)

    suspend fun getRecentOrders(limit: Int = 5): List<ProductionOrder> =
        productionOrderDataSource.getRecentOrders(limit)
}

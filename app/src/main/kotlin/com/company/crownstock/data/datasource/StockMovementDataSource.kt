package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.MovementType
import com.company.crownstock.data.model.StockMovement
import java.util.Date

/**
 * Bölüm 7 — StockMovementDataSource: Firestore "stockMovements" collection erişimi.
 * IK-9: append-only — güncelleme/silme metodu tanımlanmaz.
 */
interface StockMovementDataSource {

    // Yeni hareket kaydı ekleme (append-only)
    suspend fun addMovement(movement: StockMovement): String

    // Ürüne göre hareket geçmişi getirme (tarih aralığı, tip filtreli)
    suspend fun getMovementsByItem(
        itemId: String,
        startDate: Date? = null,
        endDate: Date? = null,
        movementType: MovementType? = null
    ): List<StockMovement>

    // Üretim emrine bağlı hareketleri getirme
    suspend fun getMovementsByProductionOrder(productionOrderId: String): List<StockMovement>
}

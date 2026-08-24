package com.company.crownstock.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class ProductionOrderStatus {
    DRAFT,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}

/**
 * Firestore "productionOrders" collection doküman modeli.
 * Bölüm 4.3.4 — Üretim emirleri (talep edilen adet, onay durumu, kullanılan stok özeti).
 *
 * IK-7: Stok düşümü yalnızca üretim onaylandığında (CONFIRMED) gerçekleşir;
 * DRAFT aşamasında ("kaç adet üretebilirim" sorgusu) stok düşülmez.
 */
data class ProductionOrder(
    @DocumentId
    val orderId: String = "",
    val targetItemId: String = "",
    val requestedQuantity: Double = 0.0,
    val status: ProductionOrderStatus = ProductionOrderStatus.DRAFT,
    val shortageDetected: Boolean = false,
    val bottleneckItemIds: List<String> = emptyList(),
    // MVP'de kullanılmaz; V2.0'da aktif olur (Bölüm 4.3.4)
    val createdBy: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    val confirmedAt: Date? = null
)

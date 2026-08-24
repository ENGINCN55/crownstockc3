package com.company.crownstock.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Bölüm 2 / IK-1: Ürünler 3 seviyeli sınıflandırılır.
 */
enum class ItemType {
    HAM_MADDE,
    YARI_MAMUL,
    NIHAI_URUN
}

/**
 * Firestore "items" collection doküman modeli.
 * Bölüm 4.3.1 — Ham madde, yarı mamül, nihai ürün master verisi.
 *
 * Not (Bölüm 4.3.1): currentStock alanı denormalizasyondur; gerçek kaynak
 * stockMovements toplamıdır. Tutarlılık Firestore transaction'ları ile sağlanır
 * (bkz. Bölüm 18 — StockRepository).
 */
data class Item(
    @DocumentId
    val itemId: String = "",
    val name: String = "",
    val itemType: ItemType = ItemType.HAM_MADDE,
    val unit: String = "",
    val currentStock: Double = 0.0,
    val minStockThreshold: Double = 0.0,
    val isActive: Boolean = true,
    val description: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null,
    // MVP'de kullanılmaz; V2.0'da aktif olur (Bölüm 4.3.1)
    val createdBy: String? = null
)

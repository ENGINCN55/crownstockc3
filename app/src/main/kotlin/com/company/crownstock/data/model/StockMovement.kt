package com.company.crownstock.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * IK-9: Her stok hareketi kalıcı olarak loglanmalıdır; loglar silinemez,
 * yalnızca eklenir (append-only).
 */
enum class MovementType {
    MANUAL_IN,
    MANUAL_OUT,
    PRODUCTION_CONSUME,
    PRODUCTION_OUTPUT,
    ADJUSTMENT
}

/**
 * Firestore "stockMovements" collection doküman modeli.
 * Bölüm 4.3.3 — Tüm stok hareketleri (giriş, çıkış, üretim düşümü, manuel düzeltme).
 *
 * Not (Bölüm 4.3.3): quantity için işaret standardı ("giriş için pozitif, çıkış için
 * negatif ya da ayrı işaretli alan") dokümanda kesin karara bağlanmamış; bu, Bölüm 18
 * StockRepository tasarımında netleştirilmelidir — burada alan tipi olarak bırakıldı.
 */
data class StockMovement(
    @DocumentId
    val movementId: String = "",
    val itemId: String = "",
    val movementType: MovementType = MovementType.ADJUSTMENT,
    val quantity: Double = 0.0,
    val resultingStock: Double = 0.0,
    val relatedProductionOrderId: String? = null,
    val reason: String? = null,
    // MVP'de kullanılmaz; V2.0'da aktif olur (Bölüm 4.3.3)
    val performedBy: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null
)

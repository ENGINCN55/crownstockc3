package com.company.crownstock.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore "bomComponents" collection doküman modeli.
 * Bölüm 4.3.2 — BOM ağacının kenarlarını (edge) temsil eder.
 * parentItemId → childItemId ilişkisi recursive'dir, seviye sınırı yoktur (Bölüm 16).
 *
 * Doğrulama kuralları (Bölüm 16 — uygulama Repository katmanında yapılmalı, burada değil):
 * - HAM_MADDE tipi bir ürün asla parentItemId olarak yer alamaz.
 * - quantityPerUnit > 0 olmalıdır.
 * - Dairesel referans (A → B → A) oluşturulamaz.
 */
data class BomComponent(
    @DocumentId
    val bomId: String = "",
    val parentItemId: String = "",
    val childItemId: String = "",
    val quantityPerUnit: Double = 0.0,
    val unit: String = "",
    val notes: String? = null,
    val isActive: Boolean = true,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

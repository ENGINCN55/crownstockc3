package com.company.crownstock.domain.model

/**
 * Bölüm 35.5 — Yeni Domain-Only Model (Firestore'a yazılmaz).
 * Bölüm 9'daki listeye ek; MaxProducibleResult/BottleneckResult/ShortageDetail
 * modelleri değiştirilmeden aynen kullanılır.
 */
data class CapacityAnalysisResult(
    val targetItemId: String,
    val requestedQuantity: Double,
    val isFullyFulfillable: Boolean,
    val maxProducibleQuantity: Double,
    val bottleneckRanking: List<BottleneckResult>,
    val missingSemiFinishedItems: List<ShortageDetail>
)

package com.company.crownstock.domain.model

/**
 * Domain-only model. Bölüm 9 — Darboğaz analizi sonucu, tek bir bileşen için.
 */
data class BottleneckResult(
    val itemId: String,
    val itemName: String,
    val availableStock: Double,
    val requiredPerUnit: Double,
    val maxUnitsSupportedByThisItem: Double
)

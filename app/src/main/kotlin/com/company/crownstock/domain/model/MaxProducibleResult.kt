package com.company.crownstock.domain.model

/**
 * Domain-only model. Bölüm 9 — Maksimum üretilebilir adet hesabının sonucu.
 */
data class MaxProducibleResult(
    val targetItemId: String,
    val maxProducibleQuantity: Double,
    val limitingItemId: String?
)

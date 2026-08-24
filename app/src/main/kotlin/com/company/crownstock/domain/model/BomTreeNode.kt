package com.company.crownstock.domain.model

import com.company.crownstock.data.model.ItemType

/**
 * Firestore'a yansımayan, yalnızca hesaplama sırasında kullanılan domain-only model.
 * Bölüm 9 / Bölüm 15.1 — BOM Ağacı Derleme algoritmasının çıktısı olan recursive
 * ağaç yapısını bellek içinde temsil eder.
 */
data class BomTreeNode(
    val itemId: String,
    val itemType: ItemType,
    val requiredQuantityForOneParent: Double,
    val children: List<BomTreeNode> = emptyList()
)

package com.company.crownstock.domain.usecase

import com.company.crownstock.data.datasource.BomDataSource
import com.company.crownstock.data.datasource.ItemDataSource
import com.company.crownstock.domain.model.ShortageDetail
import kotlin.math.max

/**
 * Bölüm 22 — Eksikler Ekranının Algoritması (Tek Seviye).
 * Girdi: targetItemId, requestedQuantity
 * Çıktı: Doğrudan (bir seviye) bileşen bazında eksik listesi.
 * Recursive DEĞİLDİR — yalnızca targetItemId'nin doğrudan bomComponents kayıtları kullanılır.
 */
class CalculateDirectShortageUseCase(
    private val bomDataSource: BomDataSource,
    private val itemDataSource: ItemDataSource
) {

    suspend fun execute(targetItemId: String, requestedQuantity: Double): List<ShortageDetail> {
        // Adım 1: yalnızca doğrudan (bir seviye) bomComponents kayıtları.
        val directComponents = bomDataSource.getComponentsByParent(targetItemId)

        val result = mutableListOf<ShortageDetail>()
        for (component in directComponents) {
            val childItem = itemDataSource.getItemById(component.childItemId) ?: continue

            // Adım 2: requiredQuantity = quantityPerUnit × requestedQuantity
            val requiredQuantity = component.quantityPerUnit * requestedQuantity
            // Adım 3: availableQuantity = component.currentStock
            val availableQuantity = childItem.currentStock
            // Adım 4: missingQuantity = max(0, requiredQuantity - availableQuantity)
            val missingQuantity = max(0.0, requiredQuantity - availableQuantity)

            // Adım 5: yalnızca missingQuantity > 0 olanlar listelenir.
            if (missingQuantity > 0) {
                result.add(
                    ShortageDetail(
                        itemId = childItem.itemId,
                        level = 1,
                        requiredQty = requiredQuantity,
                        availableQty = availableQuantity,
                        missingQty = missingQuantity,
                        itemName = childItem.name
                    )
                )
            }
        }
        return result
    }
}

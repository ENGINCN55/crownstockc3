package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.BomComponent

/**
 * Bölüm 7 — BomDataSource: Firestore "bomComponents" collection erişimi.
 */
interface BomDataSource {

    // Bir üst ürüne ait tüm alt bileşenleri getirme (parentItemId bazlı sorgu)
    suspend fun getComponentsByParent(parentItemId: String): List<BomComponent>

    // Bir bileşenin hangi üst ürünlerde kullanıldığını getirme
    // (childItemId bazlı ters sorgu — "nerede kullanılıyor" analizi için)
    suspend fun getParentsByChild(childItemId: String): List<BomComponent>

    // BOM satırı ekleme / güncelleme / pasife alma
    suspend fun addBomComponent(bomComponent: BomComponent): String
    suspend fun updateBomComponent(bomComponent: BomComponent)
    suspend fun deactivateBomComponent(bomId: String)
}
